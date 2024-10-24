package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import com.typesafe.config.Config;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class to interact with the YouTube Data API and fetch videos based on search queries.
 * Uses Play's WSClient to perform asynchronous HTTP requests.
 *
 * @author Marjan Khassafi, Deniz Dinchdonmez
 */
public class YouTubeService {

    private final String apiKey;  // Fetch API key from configuration file
    private final WSClient ws;

    /**
     * Constructor to initialize the YouTubeService with a WSClient and configuration to fetch API key.
     *
     * @param ws  Play WSClient to handle HTTP requests.
     * @param config Play Config to fetch API key from configuration.
     */
    @Inject
    public YouTubeService(WSClient ws, Config config) {
        this.ws = ws;
        // Fetch API key from configuration file
        this.apiKey = "";  // Ensure this key is present in the config file
    }

    /**
     * Searches YouTube for videos based on the given query. Fetches the latest 10 videos.
     *
     * @param query The search query (keywords) to use for finding videos.
     * @return A CompletionStage that completes with a List of Video objects.
     */
    public List<Video> searchVideos(String query) {
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url =
                String.format(
                        "%s?part=snippet&q=%s&type=video&maxResults=10&key=%s", youtubeUrl, query, apiKey);

        var futureResult = ws.url(url)
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
                                                String thumbnail =
                                                        snippet.get("thumbnails").get("default").get("url").asText();
                                                return new Video(title, description, channelId, "", thumbnail);
                                            })
                                    .collect(Collectors.toList());
                        });

        return futureResult.toCompletableFuture().join();
    }
}

