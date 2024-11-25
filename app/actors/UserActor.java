package actors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

/**
 * User actor who talks to the client and gets the information needed from others
 * @author Jessica Chen
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
                        System.out.println("Received response from YouTubeServiceActor: " + response.getVideos());
                        processReceivedResults(response);
                    })
                .build();
    }

    /**
     * Method used to process received response from YouTubeServiceActor to process it into a Json for the client
     * to read and display, and sends it to the client
     * @param response - SearchResultMessage object that contains query and list of videos
     */
    private void processReceivedResults(Messages.SearchResultsMessage response) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
            JsonNode json = objectMapper.createObjectNode()
                    .put("searchTerm", response.getSearchTerm())
                    .set("videos", objectMapper.valueToTree(response.getVideos()));
            String jsonStr = objectMapper.writeValueAsString(json);
            ws.tell(jsonStr, getSelf());
    }
}
