package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

/**
 * WebSocketActor handles communication between the client and the server over websocket connection.
 */
public class WebSocketActor extends AbstractActor {
    private final String sessionId;
    private final ActorRef youTubeServiceActor;
    private final ActorRef out;

    public static Props props(String sessionId, ActorRef out) {
        return Props.create(WebSocketActor.class, sessionId, out);
    }

    public WebSocketActor(String sessionId, ActorRef youTubeServiceActor, ActorRef out){
        this.sessionId = sessionId;
        this.youTubeServiceActor = youTubeServiceActor;
        this.out = out;
    }

    /**
     * Receives query from ws() in HomeController, forwards query to other actors for processing.
     * As well, outputs the result to the client.
     * @author Jessica Chen
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> {
                    youTubeServiceActor.tell(query, getSelf());
                    // need to fix below because getting List<Video> back right now
                    // need to out.tell(display the videos)
                    out.tell(query, getSelf());
                })
                .build();
    }
}
