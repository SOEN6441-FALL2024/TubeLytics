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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for SentimentActor class
 *
 * @author Jessica Chen
 */

public class SentimentActorTest {
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests when sentimentActor receives a valid list of videos from the UserActor to process
     *
     * @author Jessica Chen
     */

    @Test
    public void testValidVideosSentimentActor() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);
            ActorRef sentimentActor = system.actorOf(SentimentActor.props());

            List<Video> testVideos = new ArrayList<>();
            Video vid1 = new Video(
                    "cats1",
                    "happy happy happy yay happy happy sad sad",
                    "cats4days",
                    "catId",
                    "catPic",
                    "meow",
                    "today");
            Video vid2 = new Video(
                    "cats2",
                    "happy sad sad sad sad sad sad sad",
                    "cats4days",
                    "catId",
                    "catPic",
                    "meow",
                    "today");

            testVideos.add(vid1);
            testVideos.add(vid2);

            Messages.AnalyzeVideoSentiments testSearchResultMsg = new Messages.AnalyzeVideoSentiments(testVideos);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAnalysisResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAnalysisResult.class);

            assertEquals(2, results.getVideos().size());
            assertEquals(":-|", results.getSentiment());
        }};
    }

    /**
     * Tests when sentimentActor receives an invalid (null) list of videos from the UserActor to process
     *
     * @author Jessica Chen
     */
    @Test
    public void testNullVideosSentimentActor() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);
            ActorRef sentimentActor = system.actorOf(SentimentActor.props());

            List<Video> testVideos = null;

            Messages.AnalyzeVideoSentiments testSearchResultMsg = new Messages.AnalyzeVideoSentiments(testVideos);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAnalysisResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAnalysisResult.class);

            assertEquals(null, results.getVideos());
            assertEquals("N/A", results.getSentiment());
        }};
    }

    /**
     * Tests when sentimentActor receives an invalid (empty) list of videos from the UserActor to process
     *
     * @author Jessica Chen
     */
    @Test
    public void testEmptyVideosSentimentActor() {
        new TestKit(system) {{
            TestProbe senderProbe = new TestProbe(system);
            ActorRef sentimentActor = system.actorOf(SentimentActor.props());

            List<Video> testVideos = new ArrayList<>();

            Messages.AnalyzeVideoSentiments testSearchResultMsg = new Messages.AnalyzeVideoSentiments(testVideos);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAnalysisResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAnalysisResult.class);

            assertEquals(0, results.getVideos().size());
            assertEquals("N/A", results.getSentiment());
        }};
    }
}
