package actors;

import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests for YouTubeServiceActor class
 */
public class YouTubeServiceActorTest {
    private ActorSystem system;
    private WSClient mockWsClient;
    private YouTubeService mockYouTubeService;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        MockitoAnnotations.openMocks(this);
        mockWsClient = mock(WSClient.class);
        mockYouTubeService = mock(YouTubeService.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testValidSearchQuery() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);

            // Create YouTubeServiceActor with a mocked YouTubeService
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient, mockYouTubeService));

            // Mock YouTubeService to return valid video data
            List<Video> mockVideos = List.of(
                    new Video("Title 1", "Description 1", "ChannelId1", "videoId1", "Thumbnail 1", "Channel 1", "2024-01-01"),
                    new Video("Title 2", "Description 2", "ChannelId2", "videoId2", "Thumbnail 2", "Channel 2", "2024-01-02")
            );

            when(mockYouTubeService.searchVideos("test"))
                    .thenReturn(CompletableFuture.completedFuture(mockVideos));

            // Send a search query
            youTubeServiceActor.tell("test", senderProbe.ref());

            // Expect the actor to send back a SearchResultsMessage
            Messages.SearchResultsMessage message = senderProbe.expectMsgClass(Messages.SearchResultsMessage.class);
            assertEquals("test", message.getSearchTerm());
            assertEquals(2, message.getVideos().size());
        }};
    }

    @Test
    public void testErrorHandling() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);

            // Create YouTubeServiceActor with a mocked YouTubeService
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient, mockYouTubeService));

            // Mock YouTubeService to throw an exception
            when(mockYouTubeService.searchVideos("error"))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Simulated API failure")));

            // Send a search query
            youTubeServiceActor.tell("error", senderProbe.ref());

            // Expect the actor to send back an empty SearchResultsMessage
            Messages.SearchResultsMessage message = senderProbe.expectMsgClass(Messages.SearchResultsMessage.class);
            assertTrue(message.getVideos().isEmpty());
        }};
    }

    @Test
    public void testLargeResponseHandling() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);

            // Create YouTubeServiceActor with a mocked YouTubeService
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient, mockYouTubeService));

            // Mock YouTubeService to return a large list of videos
            List<Video> mockVideos = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                mockVideos.add(new Video(
                        "Title " + i,
                        "Description " + i,
                        "ChannelId " + i,
                        "videoId" + i,
                        "Thumbnail " + i,
                        "Channel " + i,
                        "2024-01-01"
                ));
            }

            when(mockYouTubeService.searchVideos("large"))
                    .thenReturn(CompletableFuture.completedFuture(mockVideos));

            // Send a search query
            youTubeServiceActor.tell("large", senderProbe.ref());

            // Expect the actor to process and send all videos
            Messages.SearchResultsMessage message = senderProbe.expectMsgClass(Messages.SearchResultsMessage.class);
            assertEquals(1000, message.getVideos().size());
        }};
    }
}
