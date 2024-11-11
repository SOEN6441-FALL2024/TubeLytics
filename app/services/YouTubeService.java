package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.ChannelInfo;
import models.Video;
import play.libs.ws.WSClient;

import javax.inject.Inject;
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
        return this.searchVideos(query,10);
    }


    public List<Video> searchVideos(String query,int limit) {
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url = String.format(
                "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s", youtubeUrl, query, limit,apiKey);

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
    /**
     * Retrieves information about a YouTube channel based on the given channel ID.
     * This includes details such as the channel's name, description, subscriber count,
     * view count, and video count.
     *
     * channelid the unique ID of the YouTube channel
     * @return a object containing the channel's information,
     *         or {@code null} if an error occurs during the API request
     * @author Aidassj
     */
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

            return null;
        }
    }
    /**
     * Retrieves the latest 10 videos from a specified YouTube channel.
     * Each video includes details such as the title, description, video ID,
     * thumbnail URL, channel title, and publication date.
     *
     * channelid the unique ID of the YouTube channel
     * @return a list of video objects representing the latest 10 videos
     *         from the channel, or an empty list if an error occurs during the API request
     *         @author Aidassj
     */
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

                }
            });

            return videos;

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}