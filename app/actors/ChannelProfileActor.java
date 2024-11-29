package actors;

import models.ChannelInfo;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.Status;
import services.YouTubeService;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor responsible for fetching and processing channel profile information
 * and the latest 10 videos of a YouTube channel using YouTube API.
 *
 * @author Aidassj
 */
public class ChannelProfileActor extends AbstractActor {
    private static final Logger logger = LoggerFactory.getLogger(ChannelProfileActor.class);

    private final YouTubeService youTubeService;

    /**
     * Constructor for ChannelProfileActor.
     *
     * @param youTubeService The service to fetch YouTube data.
     */
    public ChannelProfileActor(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    /**
     * Factory method to create Props for the ChannelProfileActor.
     *
     * @param youTubeService The YouTubeService instance.
     * @return Props for creating ChannelProfileActor instances.
     * @author Aidassj
     */
    public static Props props(YouTubeService youTubeService) {
        return Props.create(ChannelProfileActor.class, () -> new ChannelProfileActor(youTubeService));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::onFetchChannelProfile)
                .build();
    }

    /**
     * Handles the FetchChannelProfile message to fetch channel data.
     *
     * @param message the FetchChannelProfile message containing channel ID.
     * @author Aidassj
     */
    private void onFetchChannelProfile(FetchChannelProfile message) {
        logger.info("Fetching channel profile for ID: {}", message.channelId);

        // Fetch channel info and videos asynchronously
        CompletionStage<ChannelInfo> channelInfoFuture = youTubeService.getChannelInfoAsync(message.channelId)
                .exceptionally(ex -> {
                    logger.error("Failed to fetch channel info for ID: {}", message.channelId, ex);
                    return new ChannelInfo("Unavailable", "No description available", 0, 0, 0,message.channelId);
                });

        CompletionStage<List<Video>> videosFuture = youTubeService.getLast10VideosAsync(message.channelId)
                .exceptionally(ex -> {
                    logger.error("Failed to fetch videos for channel ID: {}", message.channelId, ex);
                    return List.of();
                });

        // Combine results and send them back to the sender
        channelInfoFuture.thenCombine(videosFuture, ChannelProfileData::new)
                .thenAccept(data -> {
                    logger.info("Successfully fetched data for channel ID: {}", message.channelId);
                    sender().tell(data, self());
                })
                .exceptionally(ex -> {
                    logger.error("Failed to fetch combined data for channel ID: {}", message.channelId, ex);
                    sender().tell(new Status.Failure(ex), self());
                    return null;
                });
    }

    /**
     * Message class to request channel profile data.
     * @author Aidassj
     */
    public static class FetchChannelProfile {
        /** The ID of the YouTube channel to fetch. */
        public final String channelId;

        /**
         * Constructor for FetchChannelProfile message.
         *
         * @param channelId the ID of the YouTube channel to fetch.
         * @author Aidassj
         */
        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Data class to represent the fetched channel profile information.
     * @author Aidassj
     */
    public static class ChannelProfileData {
        private final ChannelInfo channelInfo;
        private final List<Video> videos;

        /**
         * Constructor for ChannelProfileData.
         *
         * @param channelInfo The channel's profile information.
         * @param videos The list of the latest 10 videos for the channel.
         * @author Aidassj
         */
        public ChannelProfileData(ChannelInfo channelInfo, List<Video> videos) {
            this.channelInfo = channelInfo;
            this.videos = videos;
        }

        public ChannelInfo getChannelInfo() {
            return channelInfo;
        }

        public List<Video> getVideos() {
            return videos;
        }

        @Override
        public String toString() {
            return "ChannelProfileData{" +
                    "channelInfo=" + channelInfo +
                    ", videos=" + videos +
                    '}';
        }
    }
}
