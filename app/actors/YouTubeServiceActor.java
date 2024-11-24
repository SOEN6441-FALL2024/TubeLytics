package actors;
import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import play.libs.ws.WSClient;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * YouTubeServiceActor handles calls to the YoutubeApi based on given query and returns results to sender
 * @author Jessica Chen
 */
public class YouTubeServiceActor extends AbstractActor {
    private final String apiKey = "";
    private final WSClient wsClient;

    public static Props props(WSClient wsClient) {
        return Props.create(YouTubeServiceActor.class, wsClient);
    }

    @Inject
    public YouTubeServiceActor(WSClient wsClient) {
        this.wsClient = wsClient;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::handleSearchQuery)
                .build();
    }

    /**
     * Method to call searchVideosRequest method to get video results and then send it back to the
     * actor that requested it
     * @param query - inputted by the user in the browser and sent over via websockets and actors
     */
    private void handleSearchQuery(String query) {
        // to help debugging, can be deleted once done using
        System.out.println("YouTubeServiceActor receives message to search videos: " + query);
        ActorRef sender = getSender();
        CompletionStage<List<Video>> videos = searchVideosRequest(query);
        videos.whenComplete((results, error) -> {
            if (error != null) {
                System.out.println("YouTubeServiceActor sends out message with no videos: " + results);
                sender.tell(new Messages.SearchResultsMessage(query, new ArrayList<>()), getSelf());
            } else {
                System.out.println("YouTubeServiceActor sends out message with videos: " + results);
                sender.tell(new Messages.SearchResultsMessage(query, results), getSelf());
            }
        });
    }

    /**
     * Method to talk to the youtubeAPI, previously in YouTubeService
     * @param message - query
     * @return - CompletionStage list of videos
     */
    private CompletionStage<List<Video>> searchVideosRequest(String message) {
        int limit = 10;

        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search";
        String url =
               String.format(
                       "%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                       youtubeUrl, message, limit, apiKey);
        return wsClient.url(url)
               .get() // Non-blocking call to initiate the request
               .thenApply(
                       response -> {
                           // Parse the JSON response and return a list of videos
                           JsonNode items = response.asJson().get("items");
                           if (items == null || !items.isArray()) {
                               System.err.println("No items found.");
                               return new ArrayList<Video>();
                           }

                           List<Video> videos = new ArrayList<>();
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
                           return videos;
                       })
               .exceptionally(
                       e -> {
                           // Log any errors and return an empty list
                           System.err.println("Error in searchVideos: " + e.getMessage());
                           return new ArrayList<>();
                       });
   }
}
