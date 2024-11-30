package actors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.Helpers;

public class ReadabilityActorTest {

  private static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("ReadabilityActorTest");
  }

  @AfterClass
  public static void teardown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testHandleReadabilityCalculation() {
    new TestKit(system) {
      {
        // Create the actor to be tested
        final ActorRef readabilityActor = system.actorOf(ReadabilityActor.props());

        // Sample video data
        Video video1 =
            new Video(
                "Title 1",
                "This is a simple sentence.",
                "Channel1",
                "VideoID1",
                "http://example.com/thumbnail1.jpg",
                "Channel Title 1",
                "2024-11-20");
        Video video2 =
            new Video(
                "Title 2",
                "The quick brown fox jumps over the lazy dog.",
                "Channel2",
                "VideoID2",
                "http://example.com/thumbnail2.jpg",
                "Channel Title 2",
                "2024-11-20");

        List<Video> videos = Arrays.asList(video1, video2);

        // Send CalculateReadabilityMessage to the actor
        readabilityActor.tell(new Messages.CalculateReadabilityMessage(videos), getRef());

        // Expect a ReadabilityResultsMessage as a response
        Messages.ReadabilityResultsMessage response =
            expectMsgClass(Messages.ReadabilityResultsMessage.class);

        // Assert that the processed videos have the correct readability metrics
        List<Video> processedVideos = response.getVideos();

        assertEquals(2, processedVideos.size());

        Video processedVideo1 = processedVideos.get(0);
        Video processedVideo2 = processedVideos.get(1);

        assertEquals(
            Helpers.calculateFleschKincaidGradeLevel(video1.getDescription()),
            processedVideo1.getFleschKincaidGradeLevel(),
            0.01);
        assertEquals(
            Helpers.calculateFleschReadingEaseScore(video1.getDescription()),
            processedVideo1.getFleschReadingEaseScore(),
            0.01);

        assertEquals(
            Helpers.calculateFleschKincaidGradeLevel(video2.getDescription()),
            processedVideo2.getFleschKincaidGradeLevel(),
            0.01);
        assertEquals(
            Helpers.calculateFleschReadingEaseScore(video2.getDescription()),
            processedVideo2.getFleschReadingEaseScore(),
            0.01);
      }
    };
  }

  @Test
  public void testHandleEmptyVideoList() {
    new TestKit(system) {
      {
        // Create the actor to be tested
        final ActorRef readabilityActor = system.actorOf(ReadabilityActor.props());

        // Send an empty video list
        readabilityActor.tell(new Messages.CalculateReadabilityMessage(Arrays.asList()), getRef());

        // Expect a ReadabilityResultsMessage as a response
        Messages.ReadabilityResultsMessage response =
            expectMsgClass(Messages.ReadabilityResultsMessage.class);

        // Assert that the response contains an empty list
        assertNotNull(response.getVideos());
        assertTrue(response.getVideos().isEmpty());
      }
    };
  }

  @Test
  public void testHandleNullDescriptions() {
    new TestKit(system) {
      {
        // Create the actor to be tested
        final ActorRef readabilityActor = system.actorOf(ReadabilityActor.props());

        // Video with a null description
        Video video =
            new Video(
                "Title",
                null,
                "Channel1",
                "VideoID1",
                "http://example.com/thumbnail.jpg",
                "Channel Title",
                "2024-11-20");

        // Send CalculateReadabilityMessage with a video having null description
        readabilityActor.tell(
            new Messages.CalculateReadabilityMessage(Arrays.asList(video)), getRef());

        // Expect a ReadabilityResultsMessage as a response
        Messages.ReadabilityResultsMessage response =
            expectMsgClass(Messages.ReadabilityResultsMessage.class);

        // Assert that the processed video has default readability scores
        List<Video> processedVideos = response.getVideos();
        assertEquals(1, processedVideos.size());

        Video processedVideo = processedVideos.get(0);
        assertEquals(0.0, processedVideo.getFleschKincaidGradeLevel(), 0.01);
        assertEquals(0.0, processedVideo.getFleschReadingEaseScore(), 0.01);
      }
    };
  }
}
