package actors;

import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import utils.Helpers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SentimentActor used to calculate the submission sentiment for a list of videos per search result. Packages up
 * the information and sends it back to the UserActor for processing.
 *
 * @author Jessica Chen
 */
public class SentimentActor extends AbstractActor {

    public static Props props() {
        return Props.create(SentimentActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.ReadabilityResultsMessage.class, this::calculateOverallSentiment)
                .build();
    }

    /**
     * Evaluates the overall sentiment based on sentiments of each video in the list of video results from a query and
     * ensures previously calculated averageGradeLevel and averageReadingEase are stored to send back to UserActor
     *
     * @param readabilityResults message obtained from the UserActor who obtained the results from the YouTubeServiceActor
     * @author Jessica Chen
     */
    private void calculateOverallSentiment(Messages.ReadabilityResultsMessage readabilityResults) {
        ActorRef sender = getSender();
        List<Video> videos = Optional.ofNullable(readabilityResults.getVideos()).orElse(Collections.emptyList());
        double averageGradeLevel = readabilityResults.getAverageGradeLevel();
        double averageReadingEase = readabilityResults.getAverageReadingEase();
        //If video is empty then sentiment is set to "N/A"
        if (videos.isEmpty()) {
            String sentimentResult = "N/A";
            System.out.println("SentimentActor received no videos.");
            Messages.SentimentAndReadabilityResult sentimentResultEmptyVid =
                    new Messages.SentimentAndReadabilityResult(sentimentResult, videos, averageGradeLevel, averageReadingEase);
            // Adding sentiment for list of videos and sending it back to the UserActor
            sender.tell(sentimentResultEmptyVid, getSelf());
            return;
        }
        // Calls calculateHappyWordCount from Helpers class for each video in stream
        double totalHappyWordCount = videos.stream()
                .limit(50)
                .mapToDouble(video -> Helpers.calculateHappyWordCount(video.getDescription()))
                .sum();

        // Calls calculateSadWordCount from Helpers class for each video in stream
        double totalSadWordCount = videos.stream()
                .limit(50)
                .mapToDouble(video -> Helpers.calculateSadWordCount(video.getDescription()))
                .sum();

        // Calls calculateSentiment for overall sentiment calculations and setting it to searchResults
        String result = Helpers.calculateSentiment(totalHappyWordCount, totalSadWordCount);

        System.out.println("SentimentActor received and processed " + videos.size() + " videos.");
        System.out.println("SentimentActor calculated: Happy Word Count: " + totalHappyWordCount
                + ", Sad Word Count: " + totalSadWordCount + ", Overall Sentiment: " + result);

        Messages.SentimentAndReadabilityResult sentiment =
                new Messages.SentimentAndReadabilityResult(result, videos, averageGradeLevel, averageReadingEase);
        // Adding sentiment for list of videos and sending it back to the UserActor
        sender.tell(sentiment, getSelf());
    }
}
