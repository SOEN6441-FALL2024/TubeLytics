package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import java.util.List;
import java.util.Map;

// Actor for handling Tags-related operations
public class TagsActor extends AbstractActor {

    // Messages
    public static class GetVideosByTag {
        public final String tag;

        public GetVideosByTag(String tag) {
            this.tag = tag;
        }
    }
    public static class VideosByTagResponse {
        public final String tag;
        public final List<Map<String, String>> videos;

        public VideosByTagResponse(String tag, List<Map<String, String>> videos) {
            this.tag = tag;
            this.videos = videos;
        }
    }


    // Factory method to create an instance of TagsActor
    public static Props props() {
        return Props.create(TagsActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetVideosByTag.class, this::onGetVideosByTag)
                .build();
    }

    private void onGetVideosByTag(GetVideosByTag msg) {
        // Simulate fetching videos based on the tag
        List<Map<String, String>> videos = fetchVideosByTag(msg.tag);

        // Send the response back to the sender
        getSender().tell(new VideosByTagResponse(msg.tag, videos), getSelf());
    }

    private List<Map<String, String>> fetchVideosByTag(String tag) {
        // TODO: Replace this with actual logic for fetching videos
        return List.of(
                Map.of("title", "Video 1", "channel", "Channel A", "description", "Description 1"),
                Map.of("title", "Video 2", "channel", "Channel B", "description", "Description 2")
        );
    }
}
