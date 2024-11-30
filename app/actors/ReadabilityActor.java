package actors;

import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import utils.Helpers;

import java.util.List;
import java.util.stream.Collectors;

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

    private void handleReadabilityCalculation(Messages.CalculateReadabilityMessage message) {
        List<Video> processedVideos = message.getVideos().stream()
                .peek(video -> {
                    video.setFleschKincaidGradeLevel(
                            Helpers.calculateFleschKincaidGradeLevel(video.getDescription()));
                    video.setFleschReadingEaseScore(
                            Helpers.calculateFleschReadingEaseScore(video.getDescription()));
                })
                .collect(Collectors.toList());

        getSender().tell(new Messages.ReadabilityResultsMessage(processedVideos), getSelf());
    }
}
