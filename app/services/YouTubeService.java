package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Video;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class YouTubeService {

    private final String apiKey;
    private final WSClient ws;

    @Inject
    public YouTubeService(WSClient ws, Config config) {
        this.ws = ws;
        this.apiKey = "";  //your API key
    }

    public List<Video> searchVideos(String query) {
        return this.searchVideos(query, 10);
    }

    public List<Video> searchVideos(String query, int limit) {
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url = String.format(
                "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s", youtubeUrl, query, limit, apiKey);
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
                                                                snippet.get("channelTitle").asText();
                                                        String thumbnail =
                                                                snippet.get("thumbnails").get("default").get("url").asText();
                                                        return new Video(
                                                                title, description, channelId, "", thumbnail, channelTitle);
                                                    })
                                            .collect(Collectors.toList());
                                });

        return futureResult.toCompletableFuture().join();
    }

    /**
     * Fetches the latest videos associated with a specific tag asynchronously.
     *
     * @param tag The tag to search for videos by.
     * @param maxResults The maximum number of videos to return.
     * @return A CompletionStage containing a list of videos matching the tag.
     */
    public CompletionStage<List<Video>> fetchVideosByTag(String tag, int maxResults) {
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url = String.format(
                "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s", youtubeUrl, tag, maxResults, apiKey);

        return ws.url(url)
                .get()
                .thenApply(response -> {
                    JsonNode items = response.asJson().get("items");
                    return items.findValues("snippet").stream()
                            .map(snippet -> {
                                String title = snippet.get("title").asText();
                                String description = snippet.get("description").asText();
                                String channelId = snippet.get("channelId").asText();
                                String channelTitle = snippet.get("channelTitle").asText();
                                String thumbnail = snippet.get("thumbnails").get("default").get("url").asText();
                                return new Video(title, description, channelId, "", thumbnail, channelTitle);
                            })
                            .collect(Collectors.toList());
                });
    }
}
