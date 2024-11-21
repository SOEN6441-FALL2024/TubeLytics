package actors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.InvalidMessageException;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.YouTubeService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for YouTubeServiceActor class
 */
public class YouTubeServiceActorTest {
    private ActorSystem system;
    private TestKit testKit;
    private YouTubeService mockYouTubeService;
    private String query;
    private List<Video> videos;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        testKit = new TestKit(system);
        mockYouTubeService = mock(YouTubeService.class);

        query = "cat";
        // Adding mock entries into List<Video>
        videos = new ArrayList<>();
        Video video1 =
                new Video(
                        "CatVideoTitle1",
                        "CatVideoDescription1",
                        "CatVideoChannelId1",
                        "CatVideoVideoId1",
                        "CatVideoThumbnailUrl.jpg1",
                        "CatVideoChannelTitle1",
                        "2024-11-06T04:41:46Z");
        Video video2 =
                new Video(
                        "CatVideoTitle2",
                        "CatVideoDescription2",
                        "CatVideoChannelId2",
                        "CatVideoVideoId2",
                        "CatVideoThumbnailUrl.jpg2",
                        "CatVideoChannelTitle2",
                        "2024-11-06T04:41:46Z");
        videos.add(video1);
        videos.add(video2);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests youTubeServiceActor with valid query for processing
     * @author Jessica Chen
     */
    @Test
    public void testYouTubeServiceActorValidQuery() throws JsonProcessingException {
        final ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockYouTubeService));
        when(mockYouTubeService.searchVideos(query)).thenReturn(videos);
        youTubeServiceActor.tell(query, testKit.getRef());

        ObjectMapper mapper = new ObjectMapper();
        String jsonExpected = mapper.writeValueAsString(videos);

        String jsonReceived = testKit.expectMsgClass(String.class);

        assertEquals(jsonExpected, jsonReceived);
        verify(mockYouTubeService).searchVideos(query);
    }

    /**
     * Tests youTubeServiceActor with empty query for processing
     * @author Jessica Chen
     */
    @Test
    public void testYouTubeServiceActorEmptyQuery() {
        final ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockYouTubeService));
        when(mockYouTubeService.searchVideos(query)).thenReturn(videos);
        youTubeServiceActor.tell("", testKit.getRef());

        assertEquals("Invalid query.", testKit.expectMsgClass(String.class));
        verify(mockYouTubeService, never()).searchVideos(anyString());
    }

    /**
     * Tests youTubeServiceActor with null query for processing
     * @author Jessica Chen
     */
    @Test
    public void testYouTubeServiceActorNullQuery() {
        final ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockYouTubeService));
        try {
            youTubeServiceActor.tell(null, testKit.getRef());
            fail("InvalidMessageException should be thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidMessageException);
        }
    }
}
