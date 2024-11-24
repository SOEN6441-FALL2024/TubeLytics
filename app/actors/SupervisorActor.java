package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import play.libs.ws.WSClient;

import javax.inject.Inject;

/**
 * SupervisorActor who acts as supervisor for all other actors
 * @author Jessica Chen
 */
public class SupervisorActor extends AbstractActor {
    private final ActorRef userActor;
    private final ActorRef youtubeServiceActor;
    private final ActorRef wsOut;
    private final WSClient wsClient;

    public static Props props(ActorRef wsOut, WSClient wsClient) {
        return Props.create(SupervisorActor.class, wsOut, wsClient);
    }

    @Inject
    public SupervisorActor(ActorRef wsOut, WSClient wsClient) {
        this.wsOut = wsOut;
        this.youtubeServiceActor = getContext().actorOf(YouTubeServiceActor.props(wsClient), "youTubeServiceActor");
        this.userActor = getContext().actorOf(UserActor.props(wsOut, youtubeServiceActor),"userActor");
        this.wsClient = wsClient;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    userActor.tell(message, getSelf());
                    System.out.println("Supervisor Actor receives message for userActor: " + message);
                })
                .matchAny(message -> {
                    System.out.println("Supervisor Actor receives message: " + message);
                })
                .build();
    }

}
