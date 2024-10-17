package services;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

/**
 * Service class to interact with the YouTube Data API and fetch videos based on search queries.
 * Uses Play's WSClient to perform asynchronous HTTP requests.
 *
 * @author Marjan Khassafi, Deniz Dinchdonmez
 */
public class YouTubeService {

  private final String API_KEY = "AIzaSyAeSvvGH1fA3f57nH-W2HI-ZUYebsYq-KA";
  private final String YT_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
  private final WSClient ws;

  /**
   * Constructor to initialize the YouTubeService with a WSClient.
   *
   * @param ws Play WSClient to handle HTTP requests.
   */
  @Inject
  public YouTubeService(WSClient ws) {
    this.ws = ws;
  }

  /**
   * Searches YouTube for videos based on the given query. Fetches the latest 10 videos.
   *
   * @param query The search query (keywords) to use for finding videos.
   * @return A CompletionStage that completes with a List of Video objects.
   */
  public CompletionStage<List<Video>> searchVideos(String query) {
    String url =
        String.format(
            "%s?part=snippet&q=%s&type=video&maxResults=10&key=%s", YT_SEARCH_URL, query, API_KEY);


    // TODO I could have modified the logic to make it compilable, please double check, leaving full responsibility on you @Marjan1371
    return ws.url(url)
            .get()
            .thenApply(
                    response -> {
                      // Process JSON response
                      JsonNode items = response.asJson().get("items");
                      return items.findValues("snippet").stream()
                              .map(
                                      snippet -> {
                                        String title = snippet.get("title").asText();
                                        String description = snippet.get("description").asText();
                                        String channelId = snippet.get("channelId").asText();
                                        String videoId = snippet.get("resourceId").get("videoId").asText();
                                        String thumbnail =
                                                snippet.get("thumbnails").get("default").get("url").asText();
                                        return new Video(title, description, channelId, videoId, thumbnail);
                                      })
                              .collect(Collectors.toList());
                    });
  }
}
