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
import utils.Helpers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
            double avgGradeLevel = 3.14;
            double avgReadingEase = 1.59;
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

            long HappyWordCount = 0;
            long SadWordCount = 0;

            for (Video video : testVideos) {
                HappyWordCount += Helpers.calculateSadWordCount(video.getDescription());
                SadWordCount+=Helpers.calculateHappyWordCount(video.getDescription());
            }
            String actualSentiment = Helpers.calculateSentiment(HappyWordCount, SadWordCount);
            Messages.ReadabilityResultsMessage testSearchResultMsg =
                    new Messages.ReadabilityResultsMessage(testVideos, avgGradeLevel, avgReadingEase);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAndReadabilityResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAndReadabilityResult.class);

            assertEquals(testVideos.size(), results.getVideos().size());
            assertEquals(actualSentiment, results.getSentiment());
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
            double avgGradeLevel = 3.14;
            double avgReadingEase = 1.59;

            List<Video> testVideos = null;

            long HappyWordCount = 0;
            long SadWordCount = 0;

            String actualSentiment = "N/A";

            Messages.ReadabilityResultsMessage testSearchResultMsg =
                    new Messages.ReadabilityResultsMessage(testVideos, avgGradeLevel, avgReadingEase);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAndReadabilityResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAndReadabilityResult.class);

            assertTrue("testVideos should be empty.", results.getVideos().isEmpty());
            assertEquals(actualSentiment, results.getSentiment());
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
            double avgGradeLevel = 3.14;
            double avgReadingEase = 1.59;
            String lastSearchTerm = "cats";

            List<Video> testVideos = new ArrayList<>();

            String actualSentiment = "N/A";
            Messages.ReadabilityResultsMessage testSearchResultMsg =
                    new Messages.ReadabilityResultsMessage(testVideos, avgGradeLevel, avgReadingEase);
            sentimentActor.tell(testSearchResultMsg, senderProbe.ref());

            Messages.SentimentAndReadabilityResult results =
                    senderProbe.expectMsgClass(Messages.SentimentAndReadabilityResult.class);

            assertEquals(testVideos.size(), results.getVideos().size());
            assertEquals(actualSentiment, results.getSentiment());
        }};
    }
}
