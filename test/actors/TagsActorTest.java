package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import services.YouTubeService;
import models.Video;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Duration;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

public class TagsActorTest {

    private ActorSystem system;
    private YouTubeService mockYouTubeService;
    private ActorRef tagsActor;

    @Before
    public void setUp() {
        system = ActorSystem.create("TestSystem");
        mockYouTubeService = Mockito.mock(YouTubeService.class);
        tagsActor = system.actorOf(TagsActor.props(mockYouTubeService), "tagsActor");
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Test fetching videos with a valid tag.
     */
    @Test
    public void testFetchTags_ValidTag() {
        new TestKit(system) {{
            // Arrange
            String testTag = "testTag";
            List<Video> mockVideos = List.of(
                    new Video("Title1", "Description1", "Channel1", "VideoId1", "ThumbnailUrl1", "ChannelTitle1", "2024-11-06T04:41:46Z"),
                    new Video("Title2", "Description2", "Channel2", "VideoId2", "ThumbnailUrl2", "ChannelTitle2", "2024-11-06T04:41:46Z")
            );
            Mockito.when(mockYouTubeService.getVideosByTag(eq(testTag), eq(10)))
                    .thenReturn(CompletableFuture.completedFuture(mockVideos));

            // Act
            tagsActor.tell(new Messages.FetchTagsMessage(testTag), getRef());

            // Assert
            Messages.TagsResultsMessage result = expectMsgClass(Messages.TagsResultsMessage.class);
            assertNotNull(result);
            assertEquals(2, result.getVideos().size());
            assertEquals("Title1", result.getVideos().get(0).getTitle());
        }};
    }

    /**
     * Test fetching videos with an empty tag.
     */
    @Test
    public void testFetchTags_EmptyTag() {
        new TestKit(system) {{
            // Arrange
            String emptyTag = "";
            Mockito.when(mockYouTubeService.getVideosByTag(eq(emptyTag), eq(10)))
                    .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

            // Act
            tagsActor.tell(new Messages.FetchTagsMessage(emptyTag), getRef());

            // Assert
            Messages.TagsResultsMessage result = expectMsgClass(Messages.TagsResultsMessage.class);
            assertNotNull(result);
            assertTrue(result.getVideos().isEmpty());
        }};
    }

    /**
     * Test handling null tag.
     */
    @Test
    public void testFetchTags_NullTag() {
        new TestKit(system) {{
            // Arrange
            String nullTag = null;
            Mockito.when(mockYouTubeService.getVideosByTag(eq(nullTag), eq(10)))
                    .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

            // Act
            tagsActor.tell(new Messages.FetchTagsMessage(nullTag), getRef());

            // Assert
            Messages.TagsResultsMessage result = expectMsgClass(Messages.TagsResultsMessage.class);
            assertNotNull(result);
            assertTrue(result.getVideos().isEmpty());
        }};
    }

    /**
     * Test when the YouTubeService fails to fetch videos.
     */
    @Test
    public void testFetchTags_ServiceError() {
        new TestKit(system) {{
            // Arrange
            YouTubeService mockYouTubeService = mock(YouTubeService.class);
            when(mockYouTubeService.getVideosByTag("testTag", 10))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Simulated error")));

            ActorRef tagsActor = system.actorOf(TagsActor.props(mockYouTubeService));

            // Act
            tagsActor.tell(new Messages.FetchTagsMessage("testTag"), getRef());

            // Assert
            Messages.TagsResultsMessage result = expectMsgClass(
                    Duration.ofSeconds(5), Messages.TagsResultsMessage.class);

            assertNotNull(result);
            assertTrue(result.getVideos().isEmpty()); // Expecting an empty list due to the error
        }};
    }

    /**
     * Test sending an unexpected message to the actor.
     */
    @Test
    public void testUnexpectedMessage() {
        new TestKit(system) {{
            // Act
            tagsActor.tell("UnexpectedMessage", getRef());

            // Assert
            expectNoMessage();
        }};
    }
}
