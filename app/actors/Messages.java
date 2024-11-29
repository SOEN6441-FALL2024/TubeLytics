package actors;

import models.ChannelInfo;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Actor for handling the fetching of channel profile data and the last 10 videos asynchronously.
 *
 * @author Aidassj
 */
public class ChannelProfileActor extends AbstractActor {

    private final YouTubeService youTubeService;

    /**
     * Constructor to initialize the actor with a YouTubeService instance.
     *
     * @param youTubeService the service used to fetch YouTube channel data.
     * @author Aidassj
     */
    public ChannelProfileActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Factory method to create Props for the ChannelProfileActor.
     *
     * @param youTubeService the YouTubeService instance.
     * @return Props for the ChannelProfileActor.
     * @author Aidassj
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(youTubeService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::handleFetchChannelProfile)
                .build();
    }

    /**
     * Handles the FetchChannelProfile message.
     *
     * @param message the message containing the channel ID to fetch.
     * @author Aidassj
     */
    private void handleFetchChannelProfile(FetchChannelProfile message) {
        String channelId = message.getChannelId();

        CompletableFuture.supplyAsync(() -> {
                    ChannelInfo channelInfo = youTubeService.getChannelInfo(channelId);
                    List<Video> videos = youTubeService.getLast10Videos(channelId);
                    return new ChannelProfileResponse(serializeChannelData(channelInfo, videos));
                }).thenAccept(response -> sender().tell(response, self()))
                .exceptionally(ex -> {
                    sender().tell(new ErrorResponse("Failed to fetch channel profile: " + ex.getMessage()), self());
                    return null;
                });
    }

    /**
     * Serializes channel info and videos into a JSON string.
     *
     * @param channelInfo the channel information.
     * @param videos the list of videos.
     * @return a JSON string representing the serialized data.
     * @author Aidassj
     */
    private String serializeChannelData(ChannelInfo channelInfo, List<Video> videos) {
        // Replace with proper JSON serialization (e.g., Jackson or Gson)
        return "Serialized Channel Info and Videos";
    }
}
