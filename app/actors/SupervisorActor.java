package actors;

import static org.apache.pekko.actor.SupervisorStrategy.restart;
import static org.apache.pekko.actor.SupervisorStrategy.resume;
import static org.apache.pekko.actor.SupervisorStrategy.stop;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.OneForOneStrategy;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.SupervisorStrategy;
import org.apache.pekko.actor.SupervisorStrategy.Directive;
import play.libs.ws.WSClient;
import scala.runtime.AbstractPartialFunction;
import services.YouTubeService;

/**
 * SupervisorActor who acts as supervisor for all other actors
 *
 * @author Aynaz Javanivayeghan, Jessica Chen
 */
public class SupervisorActor extends AbstractActor {
  private final ActorRef userActor;
  private final ActorRef youtubeServiceActor;

  public static Props props(ActorRef wsOut, WSClient wsClient) {
    return Props.create(SupervisorActor.class, wsOut, wsClient);
  }

  public SupervisorActor(ActorRef wsOut, WSClient wsClient) {
    // Create YouTubeService instance
    YouTubeService youTubeService = new YouTubeService(wsClient, null);

    // Instantiate YouTubeServiceActor with both WSClient and YouTubeService
    this.youtubeServiceActor =
        getContext()
            .actorOf(YouTubeServiceActor.props(wsClient, youTubeService), "youTubeServiceActor");

    ActorRef readabilityActor = getContext().actorOf(ReadabilityActor.props(), "readabilityActor");

    // Create UserActor and pass the YouTubeServiceActor
    this.userActor =
        getContext()
            .actorOf(UserActor.props(wsOut, youtubeServiceActor, readabilityActor), "userActor");
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(String.class, message -> userActor.tell(message, getSelf()))
        .build();
  }

  /**
   * Processes the user input and returns the formatted result.
   *
   * @author Aynaz Javanivayeghan
   */
  @Override
  public SupervisorStrategy supervisorStrategy() {
    return new OneForOneStrategy(
        false,
        new AbstractPartialFunction<Throwable, Directive>() {
          @Override
          public Directive apply(Throwable throwable) {
            // Handle specific exceptions
            if (throwable instanceof NullPointerException) {
              System.err.println("NullPointerException occurred. Resuming actor.");
              return resume();
            } else if (throwable instanceof IllegalArgumentException) {
              System.err.println("IllegalArgumentException occurred. Restarting actor.");
              return restart();
            } else if (throwable instanceof IllegalStateException) {
              System.err.println("IllegalStateException occurred. Stopping actor.");
              return stop();
            } else {
              System.err.println("Unknown exception occurred: " + throwable.getClass().getName());
              return restart();
            }
          }

          @Override
          public boolean isDefinedAt(Throwable throwable) {
            return true; // Handle all exceptions
          }
        });
  }
}
