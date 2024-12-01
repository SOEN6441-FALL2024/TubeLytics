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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubmissionSentimentActorTest {
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

//    @Test
//    public void testSubmissionSentimentActor() {
//        new TestKit(system) {{
//            TestProbe senderProbe = new TestProbe(system);
//            ActorRef submissionSentimentActor = system.actorOf(SentimentActor.props());
//
//            String test = "cats";
//            List<Video> testVideos = List.of(new Video(
//                    "cats1",
//                    "happy happy happy yay sad sad",
//                    "cats4days",
//                    "catId",
//                    "catPic",
//                    "meow",
//                    "today"));
//
//            Messages.SearchResultsMessage testSearchResultMsg = new Messages.SearchResultsMessage(test, testVideos);
//            submissionSentimentActor.tell(testSearchResultMsg, getRef());
//
//            senderProbe.expectMsgClass(Messages.SearchResultsMessage.class);
//
//            assertEquals("cats", testSearchResultMsg.getSearchTerm());
//            assertEquals(1, testSearchResultMsg.getVideos().size());
//            assertEquals(":-)", testSearchResultMsg.getSentiment());
//        }};
//    }

//    @Test
//    public void testCalculateOverallSentiment() {
//        new TestKit(system) {{
//            TestProbe senderProbe = new TestProbe(system);
//
//
//            // Expect the actor to send back an empty SearchResultsMessage
//            Messages.SearchResultsMessage message = senderProbe.expectMsgClass(Messages.SearchResultsMessage.class);
//            assertTrue(message.getVideos().isEmpty());
//        }};
//    }
}
