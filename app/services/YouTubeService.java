package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.ChannelInfo;
import models.Video;
import play.libs.ws.WSClient;

public class YouTubeService {

    private final String apiKey;
    private final WSClient ws;

    @Inject
    public YouTubeService(WSClient ws, Config config) {
        this.ws = ws;
        this.apiKey = "AIzaSyBPhuE2lB0jBpSoz5B0CEzGbIOjlC1rLLk";
    }

    public CompletionStage<List<Video>> searchVideos(String query, int limit) {
        // Construct the YouTube API request URL
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url =
                String.format(
                        "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                        youtubeUrl, query, limit, apiKey);

        // Make the asynchronous HTTP GET request
        return ws.url(url)
                .get() // Non-blocking call to initiate the request
                .thenApply(
                        response -> {
                            // Parse the JSON response and return a list of videos
                            JsonNode items = response.asJson().get("items");
                            List<Video> videos = new ArrayList<>();
                            if (items != null) {
                                items.forEach(
                                        item -> {
                                            JsonNode snippet = item.get("snippet");
                                            videos.add(
                                                    new Video(
                                                            snippet.get("title").asText(),
                                                            snippet.get("description").asText(),
                                                            snippet.get("channelId").asText(),
                                                            item.get("id").get("videoId").asText(),
                                                            snippet.get("thumbnails").get("default").get("url").asText(),
                                                            snippet.get("channelTitle").asText(),
                                                            snippet.get("publishedAt").asText()));
                                        });
                            }
                            return videos;
                        })
                .exceptionally(
                        e -> {
                            // Log any errors and return an empty list
                            System.err.println("Error in searchVideos: " + e.getMessage());
                            return new ArrayList<>();
                        });
    }

    public CompletionStage<List<Video>> searchVideos(String query) {
        return searchVideos(query, 10); // Default to 10 results
    }
    /**
     * Asynchronously fetches information about a YouTube channel.
     *
     * @param channelId The unique ID of the YouTube channel.
     * @return A CompletionStage containing the ChannelInfo object, or null if an error occurs.
     */
    public CompletionStage<ChannelInfo> getChannelInfoAsync(String channelId) {
        String url = String.format(
                "https://www.googleapis.com/youtube/v3/channels?part=snippet,statistics&id=%s&key=%s",
                channelId, apiKey);

        return ws.url(url)
                .get()
                .thenApply(response -> {
                    try {
                        JsonNode item = response.asJson().get("items").get(0);
                        JsonNode snippet = item.get("snippet");
                        JsonNode statistics = item.get("statistics");
                        String name = snippet.get("title").asText();
                        String description = snippet.get("description").asText();
                        int subscriberCount = statistics.get("subscriberCount").asInt();
                        int viewCount = statistics.get("viewCount").asInt();
                        int videoCount = statistics.get("videoCount").asInt();

                        return new ChannelInfo(name, description, subscriberCount, viewCount, videoCount,channelId);
                    } catch (Exception e) {
                        System.err.println("Error in getChannelInfoAsync: " + e.getMessage());
                        return null;
                    }

    })
            .exceptionally(e -> {
        System.err.println("Exception in getChannelInfoAsync: " + e.getMessage());
        return null;
                });
    }

    /**
     * Asynchronously fetches the latest 10 videos from a specified YouTube channel.
     *
     * @param channelId The unique ID of the YouTube channel.
     * @return A CompletionStage containing a list of Video objects, or an empty list if an error
     * occurs.
     * @author Aidassj
     */
    public CompletionStage<List<Video>> getLast10VideosAsync(String channelId) {
        String url = String.format(
                "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=%s&maxResults=10&order=date&type=video&key=%s",
                channelId, apiKey);

        return ws.url(url)
                .get()
                .thenApply(response -> {
                    List<Video> videos = new ArrayList<>();
                    try {
                        JsonNode items = response.asJson().get("items");
                        items.forEach(item -> {
                            JsonNode snippet = item.get("snippet");
                            try {
                                String title = snippet.get("title").asText();
                                String description = snippet.get("description").asText();
                                String videoId = item.get("id").get("videoId").asText();
                                String thumbnail = snippet.get("thumbnails").get("default").get("url").asText();
                                String channelTitle = snippet.get("channelTitle").asText();
                                String publishedDate = snippet.get("publishedAt").asText();

                                videos.add(new Video(
                                        title,
                                        description,
                                        channelId,
                                        videoId,
                                        thumbnail,
                                        channelTitle,
                                        publishedDate));
                            } catch (Exception e) {
                                System.err.println("Error processing video: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error in getLast10VideosAsync: " + e.getMessage());
                    }
                    return videos;
                })
                .exceptionally(e -> {
                    System.err.println("Exception in getLast10VideosAsync: " + e.getMessage());
                    return new ArrayList<>();
                });
    }

    public CompletionStage<Video> getVideoDetails(String videoId) {
        String url =
                "https://www.googleapis.com/youtube/v3/videos"
                        + "?part=snippet"
                        + "&id="
                        + videoId
                        + "&key="
                        + apiKey;

        return ws.url(url)
                .get()
                .thenApply(
                        response -> {
                            JsonNode json = response.asJson();
                            JsonNode items = json.get("items");

                            if (items != null && items.size() > 0) {
                                JsonNode snippet = items.get(0).get("snippet");

                                String title = snippet.get("title").asText();
                                String description = snippet.get("description").asText();
                                JsonNode tagsNode = snippet.get("tags");
                                List<String> tags = new ArrayList<>();

                                if (tagsNode != null) {
                                    tagsNode.forEach(tag -> tags.add(tag.asText()));
                                }

                                String channelId = snippet.get("channelId").asText();
                                String channelTitle = snippet.get("channelTitle").asText();
                                String thumbnailUrl = snippet.get("thumbnails").get("default").get("url").asText();

                                String publishedDate =
                                        snippet.has("publishedAt") ? snippet.get("publishedAt").asText() : null;

                                Video video =
                                        new Video(
                                                title,
                                                description,
                                                channelId,
                                                videoId,
                                                thumbnailUrl,
                                                channelTitle,
                                                publishedDate);
                                video.setTags(tags);
                                return video;
                            }
                            return null;
                        });
    }

    public CompletionStage<List<Video>> searchVideosByTag(String tag) {
        String url =
                "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet"
                        + "&maxResults=10"
                        + "&q="
                        + tag
                        + "&type=video"
                        + "&key="
                        + apiKey;

        return ws.url(url)
                .get()
                .thenApply(
                        response -> {
                            JsonNode json = response.asJson();
                            List<Video> videos = new ArrayList<>();
                            JsonNode items = json.get("items");

                            if (items != null) {
                                items.forEach(
                                        item -> {
                                            JsonNode snippet = item.get("snippet");
                                            String videoId = item.get("id").get("videoId").asText();
                                            String title = snippet.get("title").asText();
                                            String description = snippet.get("description").asText();
                                            String channelId = snippet.get("channelId").asText();
                                            String channelTitle = snippet.get("channelTitle").asText();
                                            String thumbnailUrl =
                                                    snippet.get("thumbnails").get("default").get("url").asText();

                                            String publishedDate =
                                                    snippet.has("publishedAt") ? snippet.get("publishedAt").asText() : null;

                                            Video video =
                                                    new Video(
                                                            title,
                                                            description,
                                                            channelId,
                                                            videoId,
                                                            thumbnailUrl,
                                                            channelTitle,
                                                            publishedDate);
                                            // video.setTags(Collections.emptyList());
                                            videos.add(video); // adding video to list
                                        });
                            }

                            return videos;
                        });
    }
}
