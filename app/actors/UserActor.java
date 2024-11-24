package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

import java.util.List;

/**
 * User actor who talks to the client and gets the information needed from others
 */
public class UserActor extends AbstractActor {
    private final ActorRef ws;
    private final ActorRef youTubeServiceActor;

    @Inject
    public UserActor(final ActorRef wsOut, final ActorRef youTubeServiceActor) {
        this.ws = wsOut;
        this.youTubeServiceActor = youTubeServiceActor;
    }
    public static Props props(final ActorRef wsOut, final ActorRef youTubeServiceActor) {
        return Props.create(UserActor.class, wsOut, youTubeServiceActor);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                        System.out.println("Received message in YouTubeServiceActor: " + message);
                        youTubeServiceActor.tell(message, getSelf());
                })
                .match(Messages.SearchResultsMessage.class, response -> {
                    try {
                        System.out.println("Received response from YouTubeServiceActor: " + response.getVideos());
                        processReceivedResults(response);
                    } catch (Exception e) {
                        System.err.println("Error Sending JSON response: " + e.getMessage());
                    }


                })
                .build();
    }

    private void processReceivedResults(Messages.SearchResultsMessage response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode json = objectMapper.createObjectNode()
                    .put("searchTerm", response.getSearchTerm())
                    .set("videos", objectMapper.valueToTree(response.getVideos()));

            System.out.println("UserActor sending JSON response: " + json);
            String jsonStr = objectMapper.writeValueAsString(json);
            ws.tell(jsonStr, getSelf());
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }
}
