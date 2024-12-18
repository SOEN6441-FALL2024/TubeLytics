package actors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import actors.ChannelProfileActor.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.ChannelInfo;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.YouTubeService;

/**
 * Unit tests for the ChannelProfileActor class using Pekko.
 * @author Aidassj
 */
public class ChannelProfileActorTest {

  private static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("ChannelProfileActorTestSystem");
  }

  @AfterClass
  public static void teardown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  /**
   * Verifies that ChannelProfileActor fetches channel info and the last 10 videos correctly
   * using a mocked YouTubeService. Asserts the response matches the mocked data.
   * @author Aidassj
   */
  @Test
  public void testFetchChannelProfile_Success() {
    new TestKit(system) {
      {
        // Mock the YouTubeService
        YouTubeService mockYouTubeService = mock(YouTubeService.class);

        // Mock ChannelInfo and Video data
        ChannelInfo mockChannelInfo =
            new ChannelInfo("Mock Channel", "Mock Description", 1000, 50000, 200, "mockChannelId");

        Video mockVideo =
            new Video(
                "Mock Video Title",
                "Mock Video Description",
                "mockChannelId",
                "mockVideoId",
                "http://mockthumbnail.com",
                "Mock Channel",
                "2024-01-01");

        List<Video> mockVideos = List.of(mockVideo);

        // Mock service methods
        when(mockYouTubeService.getChannelInfoAsync("mockChannelId"))
            .thenReturn(CompletableFuture.completedFuture(mockChannelInfo));
        when(mockYouTubeService.getLast10VideosAsync("mockChannelId"))
            .thenReturn(CompletableFuture.completedFuture(mockVideos));

        // Create the actor
        ActorRef channelProfileActor =
            system.actorOf(ChannelProfileActor.props(mockYouTubeService));

        // Send FetchChannelProfile message
        channelProfileActor.tell(new FetchChannelProfile("mockChannelId"), getRef());

        // Expect a ChannelProfileData response
        ChannelProfileData response = expectMsgClass(ChannelProfileData.class);

        // Assertions
        assertNotNull(response);
        assertEquals(mockChannelInfo, response.getChannelInfo());
        assertEquals(mockVideos, response.getVideos());
      }
    };
  }

  /**
   * Verifies that ChannelProfileActor handles API failures gracefully.
   * Asserts that fallback values are returned for channel info and an empty video list is sent.
   * @author Aidassj
   */

  @Test
  public void testFetchChannelProfile_Failure() {
    new TestKit(system) {
      {
        // Mock the YouTubeService
        YouTubeService mockYouTubeService = mock(YouTubeService.class);

        // Mock exception behavior
        when(mockYouTubeService.getChannelInfoAsync("mockChannelId"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API failure")));
        when(mockYouTubeService.getLast10VideosAsync("mockChannelId"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API failure")));

        // Create the actor
        ActorRef channelProfileActor =
            system.actorOf(ChannelProfileActor.props(mockYouTubeService));

        // Send FetchChannelProfile message
        channelProfileActor.tell(new FetchChannelProfile("mockChannelId"), getRef());

        // Expect a ChannelProfileData response with fallback values
        ChannelProfileData response = expectMsgClass(ChannelProfileData.class);

        // Assertions
        assertNotNull(response);
        assertEquals("Unavailable", response.getChannelInfo().getName());
        assertEquals("No description available", response.getChannelInfo().getDescription());
        assertTrue(response.getVideos().isEmpty());
      }
    };
  }

  /**
   * Verifies that ChannelProfileActor handles partial API failures gracefully.
   * Asserts that channel info is returned when available, and an empty video list is sent if fetching videos fails.
   * @author Aidassj
   */
  @Test
  public void testFetchChannelProfile_PartialFailure() {
    new TestKit(system) {
      {
        // Mock the YouTubeService
        YouTubeService mockYouTubeService = mock(YouTubeService.class);

        // Simulate one service failing and the other succeeding
        ChannelInfo mockChannelInfo =
            new ChannelInfo("Mock Channel", "Mock Description", 1000, 50000, 200, "mockChannelId");
        when(mockYouTubeService.getChannelInfoAsync("mockChannelId"))
            .thenReturn(CompletableFuture.completedFuture(mockChannelInfo));
        when(mockYouTubeService.getLast10VideosAsync("mockChannelId"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API failure")));

        // Create the actor
        ActorRef channelProfileActor =
            system.actorOf(ChannelProfileActor.props(mockYouTubeService));

        // Send FetchChannelProfile message
        channelProfileActor.tell(new FetchChannelProfile("mockChannelId"), getRef());

        // Expect a ChannelProfileData response with partial success
        ChannelProfileData response = expectMsgClass(ChannelProfileData.class);

        // Assertions
        assertNotNull(response);
        assertEquals(mockChannelInfo, response.getChannelInfo());
        assertTrue(response.getVideos().isEmpty());
      }
    };
  }

  /**
   * Tests the toString method of ChannelProfileData to ensure it provides a correctly formatted string representation.
   * Verifies that the method includes the channel information and video list as expected.
   * @author Aidassj
   */
  @Test
  public void testChannelProfileDataToString() {
    // Arrange: Create mock ChannelInfo and Video list
    ChannelInfo channelInfo =
        new ChannelInfo("Test Channel", "This is a test channel.", 1000, 5000, 50, "channelId123");

    List<Video> videos =
        List.of(
            new Video(
                "Video1",
                "Description1",
                "channelId123",
                "videoId1",
                "thumbnailUrl1",
                "Test Channel",
                "2024-11-06T04:41:46Z"),
            new Video(
                "Video2",
                "Description2",
                "channelId123",
                "videoId2",
                "thumbnailUrl2",
                "Test Channel",
                "2024-11-07T04:41:46Z"));

    ChannelProfileData profileData = new ChannelProfileData(channelInfo, videos);

    // Act: Call toString
    String actualString = profileData.toString();

    // Assert: Verify the output
    String expectedString =
        "ChannelProfileData{" + "channelInfo=" + channelInfo + ", videos=" + videos + '}';

    assertEquals(expectedString, actualString);
  }
}
