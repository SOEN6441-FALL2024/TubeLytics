package actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import services.YouTubeService;

import java.util.List;

public class YouTubeServiceActor extends AbstractActor {
    private final YouTubeService youTubeService;

    public YouTubeServiceActor(YouTubeService youTubeService){
        this.youTubeService = youTubeService;
    }

    public static Props props(YouTubeService youTubeService) {
        return Props.create(YouTubeServiceActor.class, youTubeService);
    }

    /**
     * YouTubeServiceActor in charge of connecting to the API to get list of videos corresponding to the query
     * and sends it back to the actor who sent the request.
     * @author Jessica Chen
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, query -> {
                    List<Video> videos = youTubeService.searchVideos(query);
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(videos);
                    getSender().tell(json, getSelf());
                })
                .build();
    }
}
