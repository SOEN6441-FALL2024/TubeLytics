package actors;

import java.util.List;
import java.util.stream.Collectors;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import utils.Helpers;

/**
 * Actor that calculates readability metrics for a list of videos
 *
 * <p>Calculates the Flesch-Kincaid Grade Level and Flesch Reading Ease Score for each video in a
 * list of videos, then calculates the average grade level and reading ease score for the list.
 *
 * <p>Created by Deniz Dinchdonmez
 */
public class ReadabilityActor extends AbstractActor {

  public static Props props() {
    return Props.create(ReadabilityActor.class);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(Messages.CalculateReadabilityMessage.class, this::handleReadabilityCalculation)
        .build();
  }

    /**
     * Calculates readability metrics for a list of videos
     *
     * <p>Calculates the Flesch-Kincaid Grade Level and Flesch Reading Ease Score for each video in a
     * list of videos, then calculates the average grade level and reading ease score for the list.
     *
     * @param message the message containing the list of videos to process
     * @author Deniz Dinchdonmez
     */
    private void handleReadabilityCalculation(Messages.CalculateReadabilityMessage message) {
        List<Video> processedVideos = message.getVideos().stream()
                .peek(video -> {
                    video.setFleschKincaidGradeLevel(
                            Helpers.calculateFleschKincaidGradeLevel(video.getDescription()));
                    video.setFleschReadingEaseScore(
                            Helpers.calculateFleschReadingEaseScore(video.getDescription()));
                })
                .collect(Collectors.toList());

        // Calculate averages
        double averageGradeLevel = processedVideos.stream()
                .mapToDouble(Video::getFleschKincaidGradeLevel)
                .limit(50)
                .average()
                .orElse(0.0);

        double averageReadingEase = processedVideos.stream()
                .mapToDouble(Video::getFleschReadingEaseScore)
                .limit(50)
                .average()
                .orElse(0.0);

        System.out.println("ReadabilityActor processed " + processedVideos.size() + " videos.");
        System.out.println("ReadabilityActor calculated averages: Grade Level: " + averageGradeLevel
                + ", Reading Ease: " + averageReadingEase);

        // Send processed videos and averages back to UserActor
        getSender().tell(
                new Messages.ReadabilityResultsMessage(processedVideos, averageGradeLevel, averageReadingEase),
                getSelf());
    }
}
