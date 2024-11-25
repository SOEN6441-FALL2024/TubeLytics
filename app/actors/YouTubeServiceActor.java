package actors;
import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import play.libs.ws.WSClient;
import services.YouTubeService;

import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * YouTubeServiceActor handles calls to the YoutubeApi based on given query and returns results to sender
 * @author Jessica Chen
 */
public class YouTubeServiceActor extends AbstractActor {
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
        ActorRef sender = getSender();
        YouTubeService you = new YouTubeService(wsClient, null);
        CompletionStage<List<Video>> videos = you.searchVideos(query);
        videos.whenComplete((results, error) -> {
            if (error != null) {
                sender.tell(new Messages.SearchResultsMessage(query, new ArrayList<>()), getSelf());
            } else {
                sender.tell(new Messages.SearchResultsMessage(query, results), getSelf());
            }
        });
    }


}
