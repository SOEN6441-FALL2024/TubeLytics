package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Video;
import play.libs.ws.WSClient;

public class YouTubeService {

  private final String apiKey;
  private final WSClient ws;

  @Inject
  public YouTubeService(WSClient ws, Config config) {
    this.ws = ws;
    this.apiKey = "";
  }

  public List<Video> searchVideos(String query) {
    String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
    String url =
        String.format(
            "%s?part=snippet&q=%s&type=video&maxResults=10&key=%s", youtubeUrl, query, apiKey);

    var futureResult =
        ws.url(url)
            .get()
            .thenApply(
                response -> {
                  JsonNode items = response.asJson().get("items");
                  return items.findValues("snippet").stream()
                      .map(
                          snippet -> {
                            String title = snippet.get("title").asText();
                            String description = snippet.get("description").asText();
                            String channelId = snippet.get("channelId").asText();
                            String channelTitle =
                                snippet.get("channelTitle").asText(); // Fetch channel title
                            String thumbnail =
                                snippet.get("thumbnails").get("default").get("url").asText();
                            return new Video(
                                title, description, channelId, "", thumbnail, channelTitle);
                          })
                      .collect(Collectors.toList());
                });

    return futureResult.toCompletableFuture().join();
  }
}
