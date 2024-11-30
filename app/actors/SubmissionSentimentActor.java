package actors;

import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import utils.Helpers;

import java.util.List;

/**
 * SubmissionSentimentActor to calculate the submission sentiment for a list of videos per search result. Packages up
 * the information and sends it back to the UserActor for processing.
 * @author Jessica Chen
 */
public class SubmissionSentimentActor extends AbstractActor {

    public static Props props() {
        return Props.create(SubmissionSentimentActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.SearchResultsMessage.class, this::calculateOverallSentiment)
                .build();
    }

    /**
     * Evaluates the overall sentiment based on sentiments of each video in the list of video results from a query
     * @param searchResults message obtained from the UserActor who obtained the results from the YouTubeServiceActor
     */
    private void calculateOverallSentiment(Messages.SearchResultsMessage searchResults) {
        ActorRef sender = getSender();
        List<Video> videos = searchResults.getVideos();
        if (videos == null || videos.isEmpty()) {
            searchResults.setSentiment("Unavailable");
            // Adding sentiment as "Unavailable" if there are no videos in the list and sending it back to the UserActor
            sender.tell(searchResults, getSelf());
            return;
        }
        // Calls calculateHappyWordCount from Helpers class for each video in stream
        double totalHappyWordCount =
                videos.stream().limit(50).mapToDouble(video
                        -> Helpers.calculateHappyWordCount(video.getDescription())).sum();
        // Calls calculateSadWordCount from Helpers class for each video in stream
        double totalSadWordCount = videos.stream().limit(50).mapToDouble(video
                -> Helpers.calculateSadWordCount(video.getDescription())).sum();
        // Calls calculateSentiment for overall sentiment calculations and setting it to searchResults
        searchResults.setSentiment(Helpers.calculateSentiment(totalHappyWordCount, totalSadWordCount));
        // Adding sentiment for list of videos and sending it back to the UserActor
        sender.tell(searchResults, getSelf());
    }
}
