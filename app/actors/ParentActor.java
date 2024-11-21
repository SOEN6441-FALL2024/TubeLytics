package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;

/**
 * Parent actor to manage and organize all actors
 */
public class ParentActor extends AbstractActor {
    private final ActorRef youTubeServiceActor;

    public static Props props(ActorRef youTubeServiceActor) {
        return Props.create(ParentActor.class, youTubeServiceActor);
    }

    public ParentActor(ActorRef youTubeServiceActor){
        this.youTubeServiceActor = youTubeServiceActor;
    }

    /**
     * Parent actor who is in charge of managing other actors for better organization. Forwards query to actors
     * and returns back the result to the sender when ready.
     * @author Jessica Chen
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> youTubeServiceActor.tell(query, getSelf()))
                .match(String.class, json -> getSender().tell(json, getSelf()))
                .build();
    }
}
