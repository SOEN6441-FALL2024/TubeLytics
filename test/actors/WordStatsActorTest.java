package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WordStatsActorTest {

    private static ActorSystem system;

    @BeforeAll
    static void setup() {
        system = ActorSystem.create("WordStatsActorTestSystem");
    }

    @AfterAll
    static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    void testWordStatsRequestWithValidData() {
        new TestKit(system) {{
            // Create WordStatsActor
            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(), "wordStatsActor1");

            // Prepare video texts
            List<String> videoTexts = List.of(
                    "Canada is a beautiful country",
                    "Montreal is a vibrant city in Canada"
            );

            // Send WordStatsRequest to the actor
            wordStatsActor.tell(new Messages.WordStatsRequest(videoTexts), getRef());

            // Expect WordStatsResponse
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            // Verify the response contains expected word frequencies
            Map<String, Long> result = response.getWordStats().stream()
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

            assertEquals(2L, result.get("canada"));
            assertEquals(1L, result.get("montreal"));
            assertEquals(1L, result.get("beautiful"));
            assertEquals(1L, result.get("country"));
            assertEquals(1L, result.get("vibrant"));
            assertFalse(result.containsKey("is")); // Ensure stopwords are excluded
        }};
    }

    @Test
    void testWordStatsRequestWithEmptyData() {
        new TestKit(system) {{
            // Create WordStatsActor
            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(), "wordStatsActor2");

            // Send WordStatsRequest with empty video texts
            wordStatsActor.tell(new Messages.WordStatsRequest(Collections.emptyList()), getRef());

            // Expect WordStatsResponse with empty stats
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            assertTrue(response.getWordStats().isEmpty());
        }};
    }

    @Test
    void testWordStatsRequestWithNullData() {
        new TestKit(system) {{
            // Create WordStatsActor
            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(), "wordStatsActor3");

            // Send WordStatsRequest with null video texts
            wordStatsActor.tell(new Messages.WordStatsRequest(null), getRef());

            // Expect WordStatsResponse with empty stats
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            assertTrue(response.getWordStats().isEmpty());
        }};
    }

    @Test
    void testGetCumulativeStats() {
        new TestKit(system) {{
            // Create WordStatsActor
            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(), "wordStatsActor4");

            // Prepare video texts
            List<String> videoTexts = List.of(
                    "Canada is a beautiful country",
                    "Montreal is a vibrant city in Canada"
            );

            // Send WordStatsRequest to populate stats
            wordStatsActor.tell(new Messages.WordStatsRequest(videoTexts), getRef());

            // Wait for response
            expectMsgClass(Messages.WordStatsResponse.class);

            // Request cumulative stats
            wordStatsActor.tell(new Messages.GetCumulativeStats(), getRef());

            // Expect WordStatsResponse
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            // Verify cumulative stats contain correct data
            Map<String, Long> result = response.getWordStats().stream()
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

            assertEquals(2L, result.get("canada"));
            assertEquals(1L, result.get("montreal"));
            assertEquals(1L, result.get("beautiful"));
            assertEquals(1L, result.get("country"));
            assertEquals(1L, result.get("vibrant"));
        }};
    }

    @Test
    void testUnexpectedMessage() {
        new TestKit(system) {{
            // Create WordStatsActor
            ActorRef wordStatsActor = system.actorOf(WordStatsActor.props(), "wordStatsActor5");

            // Send an unexpected message
            wordStatsActor.tell("Unexpected Message", getRef());

            // Expect ErrorMessage
            Messages.ErrorMessage response = expectMsgClass(Messages.ErrorMessage.class);

            // Assert response
            assertEquals("Invalid message type", response.getMessage());
        }};
    }
}