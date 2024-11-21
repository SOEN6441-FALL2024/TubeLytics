package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

/**
 * WebSocketActor handles communication between the client and the server over websocket connection.
 */
public class WebSocketActor extends AbstractActor {
    private final String sessionId;
    private final ActorRef parentActor;
    private final ActorRef out;

    public static Props props(String sessionId, ActorRef parentActor, ActorRef out) {
        return Props.create(WebSocketActor.class, sessionId, parentActor, out);
    }

    public WebSocketActor(String sessionId, ActorRef parentActor, ActorRef out){
        this.sessionId = sessionId;
        this.parentActor = parentActor;
        this.out = out;
    }

    /**
     * Receives query from ws() in HomeController, creates actors, forwards query to other actors for processing.
     * As well, outputs the result to the client.
     * @author Jessica Chen
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> parentActor.tell(query, getSelf()))
                .match(String.class, json -> out.tell(json, getSelf()))
                .build();
    }
}
