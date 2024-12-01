package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import models.Video;
import services.YouTubeService;

import java.util.List;

/**
 * Actor responsible for fetching videos related to a specific tag.
 */
public class TagsActor extends AbstractActor {

    private final YouTubeService youTubeService;

    public TagsActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    // Factory method to create Props for this actor
    public static Props props(YouTubeService youTubeService) {
        return Props.create(TagsActor.class, () -> new TagsActor(youTubeService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.FetchTagsMessage.class, this::handleFetchTags)
                .build();
    }

    private void handleFetchTags(Messages.FetchTagsMessage message) {
        youTubeService.getVideosByTag(message.getTag(), 10) // Limit to 10 videos
                .thenAccept(videos -> {
                    sender().tell(new Messages.TagsResultsMessage(videos), self());
                })
                .exceptionally(ex -> {
                    sender().tell(new Messages.TagsResultsMessage(List.of()), self());
                    return null;
                });
    }


}
