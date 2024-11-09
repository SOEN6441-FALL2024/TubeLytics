package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import com.typesafe.config.Config;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class YouTubeService {

    private final String apiKey;
    private final WSClient ws;

    @Inject
    public YouTubeService(WSClient ws, Config config) {
        this.ws = ws;
        this.apiKey = "AIzaSyClI-qUlL1t3a924x6h4W372lii8-lPsfQ";
    }

    public List<Video> searchVideos(String query) {
        return this.searchVideos(query,10);
    }


    public List<Video> searchVideos(String query,int limit) {
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url = String.format(
                "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s", youtubeUrl, query, limit,apiKey);

        var futureResult = ws.url(url)
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
                                                String channelTitle = snippet.get("channelTitle").asText(); // Fetch channel title
                                                String thumbnail = snippet.get("thumbnails").get("default").get("url").asText();
                                                return new Video(title, description, channelId, "", thumbnail, channelTitle);
                                            })
                                    .collect(Collectors.toList());
                        });

        return futureResult.toCompletableFuture().join();
    }
}
