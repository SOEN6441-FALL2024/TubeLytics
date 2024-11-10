package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.Video;
import play.libs.ws.WSClient;
import models.ChannelInfo;
import java.util.ArrayList;
import java.util.List;

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

      var futureResult = ws.url(url)
              .get()
              .thenApply(response -> {
                  JsonNode items = response.asJson().get("items");

                  List<Video> videos = new ArrayList<>();
                  items.forEach(item -> {
                      JsonNode snippet = item.get("snippet");
                      String title = snippet.get("title").asText();
                      String description = snippet.get("description").asText();
                      String channelId = snippet.get("channelId").asText();
                      String channelTitle = snippet.get("channelTitle").asText();
                      String thumbnail = snippet.get("thumbnails").get("default").get("url").asText();
                      String publishedDate = snippet.get("publishedAt").asText();
                      String videoId = item.get("id").get("videoId").asText();

                      videos.add(new Video(title, description, channelId, videoId, thumbnail, channelTitle, publishedDate));
                  });
                  return videos;
              });

      return (List<Video>) futureResult.toCompletableFuture().join();
  }

    public ChannelInfo getChannelInfo(String channelId) {
        String url = String.format("https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=%s&key=%s", channelId, apiKey);

        try {
            JsonNode item = ws.url(url)
                    .get()
                    .toCompletableFuture()
                    .join()
                    .asJson()
                    .get("items")
                    .get(0);

            JsonNode snippet = item.get("snippet");
            JsonNode statistics = item.get("statistics");
            String name = snippet.get("title").asText();
            String description = snippet.get("description").asText();
            int subscriberCount = statistics.get("subscriberCount").asInt();
            int viewCount = statistics.get("viewCount").asInt();
            int videoCount = statistics.get("videoCount").asInt();

            return new ChannelInfo(name, description, subscriberCount, viewCount, videoCount);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Video> getLast10Videos(String channelId) {
        String url = String.format("https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=%s&maxResults=10&order=date&type=video&key=%s", channelId, apiKey);

        try {
            JsonNode items = ws.url(url)
                    .get()
                    .toCompletableFuture()
                    .join()
                    .asJson()
                    .get("items");

            List<Video> videos = new ArrayList<>();
            items.forEach(item -> {
                JsonNode snippet = item.get("snippet");
                try {
                    String title = snippet.get("title").asText();
                    String description = snippet.get("description").asText();
                    String videoId = item.get("id").get("videoId").asText();
                    String thumbnail = snippet.get("thumbnails").get("default").get("url").asText();
                    String channelTitle = snippet.get("channelTitle").asText();
                    String publishedDate = snippet.get("publishedAt").asText();

                    videos.add(new Video(title, description, channelId, videoId, thumbnail, channelTitle, publishedDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return videos;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

