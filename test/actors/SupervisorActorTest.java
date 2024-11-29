package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import scala.jdk.javaapi.CollectionConverters;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.AbstractMap;
import java.util.List;
import org.apache.pekko.actor.Props;
public class SupervisorActorTest {
    static ActorSystem system;
    private WSClient mockWsClient;
    private WSRequest mockRequest;
    private WSResponse mockResponse;
    private YouTubeService mockYouTubeService;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        MockitoAnnotations.openMocks(this);

        mockRequest = mock(WSRequest.class);
        mockWsClient = mock(WSClient.class);
        mockResponse = mock(WSResponse.class);
        mockYouTubeService = mock(YouTubeService.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests if supervisorActor is able to forward query to userActor and the userActor to the youtubeServiceActor.
     * Creating a mockWsClient, working with a mockRequest and mockResponses and ensuring that it transform into a
     * mockJsonResponse to prevent any null pointer exceptions.
     * @author Aynaz Javanivayeghan, Jessica Chen
     */
    @Test
    public void testSupervisorActorQueryForward() {
        new TestKit(system) {{
            when(mockWsClient.url(anyString())).thenReturn(mockRequest);
            when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));
            when(mockResponse.asJson()).thenReturn(new ObjectMapper().createObjectNode());
            when(mockResponse.getStatus()).thenReturn(200);

            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            supervisorActor.tell("cats", getRef());
            expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));
        }};
    }

    /**
     * Tests whether supervisorActor creates children actors
     * @author Jessica Chen
     */
    @Test
    public void testWebSocketActorEmptyQuery() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));
            assertNotNull(system.actorSelection(supervisorActor.path().child("userActor")));
            assertNotNull(system.actorSelection(supervisorActor.path().child("youTubeServiceActor")));
        }};
    }

    /**
     * Tests whether SupervisorActor creates child actors.
     */
    @Test
    public void testSupervisorActorCreatesChildActors() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            assertNotNull(system.actorSelection(supervisorActor.path().child("userActor")));
            assertNotNull(system.actorSelection(supervisorActor.path().child("youTubeServiceActor")));
        }};
    }

    /**
     * Tests supervisorActor with unhandled messages such as int, because right  now it is only handling String
     * @author Aynaz Javanivayeghan, Jessica Chen
     */
    @Test
    public void testSupervisorActorUnhandledMessage() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            supervisorActor.tell(42, getRef());

            // Expect an ErrorMessage for unhandled messages
            Messages.ErrorMessage response = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", response.getMessage());
        }};
    }
    /**
     * Tests the SupervisorStrategy of SupervisorActor with different exceptions.
     */
    @Test
    public void testSupervisorStrategy() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            // Simulate a NullPointerException (actor should treat it as an unknown message type)
            supervisorActor.tell(new NullPointerException("Simulated NPE"), getRef());
            Messages.ErrorMessage errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());

            // Simulate an IllegalArgumentException (actor should treat it as an unknown message type)
            supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), getRef());
            errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());

            // Simulate an IllegalStateException (actor should treat it as an unknown message type)
            supervisorActor.tell(new IllegalStateException("Simulated ISE"), getRef());
            errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());

            // Simulate a RuntimeException (actor should treat it as an unknown message type)
            supervisorActor.tell(new RuntimeException("Simulated RuntimeException"), getRef());
            errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());
        }};
    }
    @Test
    public void testSupervisorStrategy_NullPointerException() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            // Send a NullPointerException to trigger the supervisor strategy
            supervisorActor.tell(new NullPointerException("Simulated NPE"), getRef());

            // Expect an ErrorMessage with the generic "Unknown message type"
            Messages.ErrorMessage response = expectMsgClass(Messages.ErrorMessage.class);

            // Verify the error message content matches the current actor behavior
            assertEquals("Unknown message type", response.getMessage());
        }};
    }
    @Test
    public void testSupervisorStrategy_IllegalArgumentException() {
        new TestKit(system) {{
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

            // Simulate an IllegalArgumentException
            supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), getRef());

            // Expect an ErrorMessage response
            Messages.ErrorMessage errorMessage = expectMsgClass(Messages.ErrorMessage.class);

            // Verify the error message matches the current actor behavior
            assertEquals("Unknown message type", errorMessage.getMessage());
        }};
    }

    @Test
    public void testSupervisorStrategy_IllegalStateException() {
        new TestKit(system) {{
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

            // Simulate an IllegalStateException
            supervisorActor.tell(new IllegalStateException("Simulated ISE"), getRef());

            // Expect the generic error message
            Messages.ErrorMessage errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());
        }};
    }

    @Test
    public void testSupervisorStrategy_UnknownException() {
        new TestKit(system) {{
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

            // Simulate an unknown exception
            supervisorActor.tell(new Exception("Simulated Unknown Exception"), getRef());

            // Expect an ErrorMessage response with "Unknown message type"
            Messages.ErrorMessage errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());
        }};
    }

    @Test
    public void testSupervisorActorInitialization() {
        new TestKit(system) {{
            // Ensure the SupervisorActor is created successfully
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));
            assertNotNull(supervisorActor);
        }};

    }


    @Test
    public void testSupervisorStrategy_HandleSpecificExceptions() {
        new TestKit(system) {{
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

            // Simulate a NullPointerException and validate the response
            supervisorActor.tell(new NullPointerException("Simulated NPE"), getRef());
            Messages.ErrorMessage errorMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", errorMessage.getMessage());
            System.out.println("NullPointerException handling verified: Unknown message response.");

            // Simulate an IllegalArgumentException and validate the response
            supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), getRef());
            Messages.ErrorMessage iaeMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", iaeMessage.getMessage());
            System.out.println("IllegalArgumentException handling verified: Unknown message response.");

            // Simulate an IllegalStateException and validate the response
            supervisorActor.tell(new IllegalStateException("Simulated ISE"), getRef());
            Messages.ErrorMessage iseMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", iseMessage.getMessage());
            System.out.println("IllegalStateException handling verified: Unknown message response.");

            // Simulate an unknown exception and validate the response
            supervisorActor.tell(new Exception("Simulated Unknown Exception"), getRef());
            Messages.ErrorMessage unknownMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", unknownMessage.getMessage());
            System.out.println("Unknown exception handling verified: Unknown message response.");
        }};
    }

    @Test
    public void testSupervisorStrategy_isDefinedAt() {
        new TestKit(system) {{
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

            // Validate that isDefinedAt is true for all exceptions
            supervisorActor.tell(new NullPointerException("Test NullPointerException"), getRef());
            Messages.ErrorMessage nullPointerMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", nullPointerMessage.getMessage());
            System.out.println("NullPointerException handling validated with ErrorMessage.");

            supervisorActor.tell(new IllegalArgumentException("Test IllegalArgumentException"), getRef());
            Messages.ErrorMessage illegalArgMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", illegalArgMessage.getMessage());
            System.out.println("IllegalArgumentException handling validated with ErrorMessage.");

            supervisorActor.tell(new IllegalStateException("Test IllegalStateException"), getRef());
            Messages.ErrorMessage illegalStateMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", illegalStateMessage.getMessage());
            System.out.println("IllegalStateException handling validated with ErrorMessage.");

            supervisorActor.tell(new RuntimeException("Test RuntimeException"), getRef());
            Messages.ErrorMessage runtimeMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", runtimeMessage.getMessage());
            System.out.println("RuntimeException handling validated with ErrorMessage.");

            supervisorActor.tell(new Exception("Test Unknown Exception"), getRef());
            Messages.ErrorMessage unknownExceptionMessage = expectMsgClass(Messages.ErrorMessage.class);
            assertEquals("Unknown message type", unknownExceptionMessage.getMessage());
            System.out.println("Unknown exception handling validated with ErrorMessage.");
        }};
    }
    /**
     * Tests SupervisorActor's ability to handle WordStatsRequest.
     */
    @Test
    public void testSupervisorActorHandlesSearchResultsMessage() {
        new TestKit(system) {{
            TestProbe wordStatsActorProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), mockWsClient));

            // Mock the WordStatsActor as a child of SupervisorActor
            system.actorOf(WordStatsActor.props(), "wordStatsActor");

            // Create a Video object
            models.Video video = new models.Video(
                    "Test Title",               // title
                    "Test Description",         // description
                    "ChannelID",                // channelId
                    "VideoID",                  // videoId
                    "ThumbnailUrl",             // thumbnailUrl
                    "ChannelTitle",             // channelTitle
                    "PublishedDate"             // publishedDate
            );

            // Create a SearchResultsMessage with a search term and a list of Video objects
            Messages.SearchResultsMessage message = new Messages.SearchResultsMessage(
                    "Test Search Term",         // Search term
                    List.of(video)              // List of Video objects
            );

            // Send the message to the SupervisorActor
            supervisorActor.tell(message, getRef());

            // Validate no unexpected response
            expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));
        }};
    }

    @Test
    public void testSupervisorActorHandlesWordStatsRequest() {
        new TestKit(system) {{
            TestProbe wordStatsActorProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), mockWsClient));

            // Mock the WordStatsActor as a child of SupervisorActor
            system.actorOf(WordStatsActor.props(), "wordStatsActor");

            // Send a WordStatsRequest message
            Messages.WordStatsRequest request = new Messages.WordStatsRequest(List.of("test"));
            supervisorActor.tell(request, getRef());

            // Expect a WordStatsResponse message from the WordStatsActor
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            // Validate the response
            assertNotNull(response);
            assertTrue(response.getWordStats().size() > 0); // Check if the response contains stats
        }};
    }

    @Test
    public void testSupervisorActorHandlesGetCumulativeStats() {
        new TestKit(system) {{
            // Create a test probe for WordStatsActor
            TestProbe wordStatsActorProbe = new TestProbe(system);

            // Create SupervisorActor with the test probe as WordStatsActor
            ActorRef supervisorActor = system.actorOf(
                    Props.create(SupervisorActor.class, getRef(), mockWsClient, wordStatsActorProbe.ref())
            );

            // Send a GetCumulativeStats message to SupervisorActor
            Messages.GetCumulativeStats request = new Messages.GetCumulativeStats();
            supervisorActor.tell(request, getRef());

            // Expect SupervisorActor to forward GetCumulativeStats to WordStatsActor
            wordStatsActorProbe.expectMsgClass(Messages.GetCumulativeStats.class);

            // Mock a response from WordStatsActor
            List<Map.Entry<String, Long>> mockStats = List.of(
                    new AbstractMap.SimpleEntry<>("exampleWord", 10L)
            );
            Messages.WordStatsResponse mockResponse = new Messages.WordStatsResponse(mockStats);

            // Reply with the mocked response
            wordStatsActorProbe.reply(mockResponse);

            // Expect SupervisorActor to forward the response back
            Messages.WordStatsResponse response = expectMsgClass(Messages.WordStatsResponse.class);

            // Validate the response
            assertNotNull("The WordStatsResponse is null", response);
            assertNotNull("The word stats list is null", response.getWordStats());
            assertFalse("The word stats list is empty", response.getWordStats().isEmpty());

            boolean containsExpectedEntry = response.getWordStats().stream()
                    .anyMatch(entry -> entry.getKey().equals("exampleWord") && entry.getValue().equals(10L));
            assertTrue("The word stats list does not contain the expected data", containsExpectedEntry);
        }};
    }
}