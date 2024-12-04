package actors;

import static org.apache.pekko.actor.SupervisorStrategy.restart;
import static org.apache.pekko.actor.SupervisorStrategy.resume;
import static org.apache.pekko.actor.SupervisorStrategy.stop;
import actors.TagsActor.VideosByTagResponse;



import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.OneForOneStrategy;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.SupervisorStrategy;
import org.apache.pekko.actor.SupervisorStrategy.Directive;
import play.libs.ws.WSClient;
import scala.runtime.AbstractPartialFunction;
import services.YouTubeService;
import actors.TagsActor.GetVideosByTag;


import java.util.List;
import java.util.stream.Collectors;

/**
 * SupervisorActor who acts as supervisor for all other actors
 *
 * @author
 */
public class SupervisorActor extends AbstractActor {
    private final ActorRef userActor;
    private final ActorRef youtubeServiceActor;
    private final ActorRef wordStatsActor;
    private final ActorRef tagsActor;

    public static Props props(ActorRef wsOut, WSClient wsClient) {
        return Props.create(SupervisorActor.class, wsOut, wsClient);
    }

    public SupervisorActor(ActorRef wsOut, WSClient wsClient) {
        // Create YouTubeService instance
        YouTubeService youTubeService = new YouTubeService(wsClient, null);
        this.wordStatsActor = getContext().actorOf(WordStatsActor.props(), "wordStatsActor");

        // Instantiate YouTubeServiceActor with both WSClient and YouTubeService
        this.youtubeServiceActor =
                getContext()
                        .actorOf(YouTubeServiceActor.props(wsClient, youTubeService), "youTubeServiceActor");

        ActorRef readabilityActor = getContext().actorOf(ReadabilityActor.props(), "readabilityActor");

        ActorRef sentimentActor = getContext().actorOf(SentimentActor.props(), "sentimentActor");

        // Create UserActor and pass the YouTubeServiceActor
        this.userActor =
                getContext()
                        .actorOf(UserActor.props(wsOut, youtubeServiceActor, readabilityActor, sentimentActor), "userActor");

        // Create TagsActor
        this.tagsActor = getContext().actorOf(TagsActor.props(), "tagsActor");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> userActor.tell(message, getSelf()))
                .match(Messages.SearchResultsMessage.class, message -> {
                    // Extract video texts (title + description) from the search results
                    List<String> videoTexts = message.getVideos().stream()
                            .map(video -> video.getTitle() + " " + video.getDescription())
                            .collect(Collectors.toList());

                    // Send WordStatsRequest to WordStatsActor
                    System.out.println("SupervisorActor: Sending WordStatsRequest to WordStatsActor with " + videoTexts.size() + " video texts.");
                    wordStatsActor.tell(new Messages.WordStatsRequest(videoTexts), getSelf());
                })
                .match(Messages.WordStatsRequest.class, request -> {
                    // Forward WordStatsRequest to the WordStatsActor
                    System.out.println("SupervisorActor: Forwarding WordStatsRequest to WordStatsActor.");
                    wordStatsActor.forward(request, getContext());
                })
                .match(Messages.GetCumulativeStats.class, request -> {
                    // Forward GetCumulativeStats to WordStatsActor
                    System.out.println("SupervisorActor: Forwarding GetCumulativeStats to WordStatsActor.");
                    wordStatsActor.forward(request, getContext());
                })
                .match(Messages.WordStatsResponse.class, response -> {
                    // Handle WordStatsResponse and forward it
                    System.out.println("SupervisorActor: Received WordStatsResponse: " + response.getWordStats());
                    getSender().tell(response, getSelf());
                })
                .match(TagsActor.GetVideosByTag.class, request -> {
                    System.out.println("SupervisorActor: Forwarding GetVideosByTag to TagsActor.");
                    tagsActor.forward(request, getContext());
                })

                .match(TagsActor.VideosByTagResponse.class, response -> {
                    System.out.println("SupervisorActor: Received VideosByTagResponse for tag: " + response.tag);
                    getSender().tell(response, getSelf());
                })


                .match(IllegalStateException.class, exception -> {
                    throw exception; // Propagate the exception to trigger the supervisor strategy
                })
                .match(Exception.class, exception -> {
                    // Log the exception and handle gracefully
                    System.err.println("SupervisorActor: Caught unknown exception: " + exception.getMessage());
                })
                .matchAny(message -> {
                    // Handle unexpected messages
                    System.err.println("SupervisorActor: Received unexpected message: " + message);
                    getSender().tell(new Messages.ErrorMessage("Unknown message type"), getSelf());
                })
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
