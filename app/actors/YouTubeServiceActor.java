package actors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import play.libs.ws.WSClient;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

/**
 * YouTubeServiceActor handles calls to the YoutubeApi based on given query and returns results to
 * sender
 *
 * @author Aidassj, Jessica Chen
 */
public class YouTubeServiceActor extends AbstractActor {
  private final YouTubeService youTubeService;
  private final Set<String> processedVideoIds = new HashSet<>();
  private static final int UPDATE_INTERVAL_SECONDS = 10;

  public static Props props(WSClient wsClient, YouTubeService youTubeService) {
    return Props.create(YouTubeServiceActor.class, () -> new YouTubeServiceActor(youTubeService));
  }

  public YouTubeServiceActor(YouTubeService youTubeService) {
    this.youTubeService = youTubeService;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder().match(String.class, this::handleSearchQuery).build();
  }

  /**
   * Method to call searchVideosRequest method to get video results and then send it back to the
   * actor that requested it
   * @author Aidassj
   */
  private void handleSearchQuery(String query) {
    ActorRef sender = getSender();

    // Schedule periodic updates for the search query
    getContext()
        .getSystem()
        .scheduler()
        .scheduleWithFixedDelay(
            Duration.Zero(),
            Duration.create(UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS), // Poll every 10 seconds
            () -> fetchAndStreamResults(query, sender),
            getContext().dispatcher());
  }

  /**
   * Fetches video results for a query and sends unique results to the sender.
   * Filters duplicates and handles errors by sending an empty result set.
   * @param query the search query
   * @param sender the actor to send results to
   * @author Aidassj
   */

  private void fetchAndStreamResults(String query, ActorRef sender) {
    CompletionStage<List<Video>> videos = youTubeService.searchVideos(query);

    videos.whenComplete(
        (results, error) -> {
          if (error != null) {
            System.err.println(
                "Error fetching videos for query '" + query + "': " + error.getMessage());
            sender.tell(new Messages.SearchResultsMessage(query, new ArrayList<>()), getSelf());
          } else {
            // Filter out already processed videos
            List<Video> newResults =
                results.stream()
                    .filter(video -> !processedVideoIds.contains(video.getVideoId()))
                    .collect(Collectors.toList());

            newResults.forEach(video -> processedVideoIds.add(video.getVideoId()));

            if (!newResults.isEmpty()) {
              System.out.println(
                  "YouTubeServiceActor sending "
                      + newResults.size()
                      + " results for query: "
                      + query);
              sender.tell(new Messages.SearchResultsMessage(query, newResults), getSelf());
            } else {
              System.out.println("No new results for query: " + query);
            }
          }
        });
  }
}
