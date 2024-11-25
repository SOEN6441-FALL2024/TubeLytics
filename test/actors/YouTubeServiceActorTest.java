package actors;

import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.YouTubeService;

import java.util.concurrent.CompletableFuture;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for YouTubeServiceActor class
 */
public class YouTubeServiceActorTest {
    private ActorSystem system;
    private WSClient mockWsClient;
    private WSRequest mockRequest;
    private WSResponse mockResponse;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        MockitoAnnotations.openMocks(this);

        mockRequest = mock(WSRequest.class);
        mockWsClient = mock(WSClient.class);
        mockResponse = mock(WSResponse.class);

        when(mockWsClient.url(anyString())).thenReturn(mockRequest);
        when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }
    /**
     * From YouTubeService but adjusted to actor.
     */
    @Test
    public void testYouTubeServiceActorValidItem() {
        new TestKit(system) {{
            TestProbe userActorProbe = new TestProbe(system);
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient));

            // Mocking JSON response
            String responseBody =
                    "{\"items\": [{\"snippet\": {\"title\": \"Test Video\", \"description\": \"Test Description\", \"channelId\": \"testChannel\", \"channelTitle\": \"Test Channel\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\"}, \"id\": {\"videoId\": \"videoId123\"}}]}";
            JsonNode mockJson = Json.parse(responseBody);
            when(mockResponse.asJson()).thenReturn(mockJson);

            // Setting up WSClient to return mocked request and response
            when(mockWsClient.url(anyString())).thenReturn(mockRequest);
            when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

            String query = "test";
            youTubeServiceActor.tell(query, userActorProbe.ref());

            Messages.SearchResultsMessage resultMessage =
                    userActorProbe.expectMsgClass(Messages.SearchResultsMessage.class);

            assertEquals(query, resultMessage.getSearchTerm());
            assertEquals("Test Video", resultMessage.getVideos().get(0).getTitle());
            assertEquals("Test Description", resultMessage.getVideos().get(0).getDescription());
            assertEquals("testChannel", resultMessage.getVideos().get(0).getChannelId());
            assertEquals("videoId123", resultMessage.getVideos().get(0).getVideoId());
            assertEquals("thumbnailUrl", resultMessage.getVideos().get(0).getThumbnailUrl());
            assertEquals("2024-11-06T04:41:46Z", resultMessage.getVideos().get(0).getPublishedDate());
        }};
    }

    @Test
    public void testYouTubeServiceActorItemNotArray() throws Exception {
        new TestKit(system) {{
            // Creating a search query that is expected to return no results
            String nonexistentQuery = "nonexistentquery1234567890";

            TestProbe userActorProbe = new TestProbe(system);
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient));
            try {
                // Mocking an empty JSON response
                String responseBody = "{\"items\": \"Iamnotanarray\"}";
                JsonNode mockJson = Json.parse(responseBody);
                when(mockResponse.asJson()).thenReturn(mockJson);

                // Setting up WSClient to return the mocked request and response
                when(mockWsClient.url(anyString())).thenReturn(mockRequest);
                when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

                youTubeServiceActor.tell(nonexistentQuery, userActorProbe.ref());

                Messages.SearchResultsMessage resultMessage =
                        userActorProbe.expectMsgClass(Messages.SearchResultsMessage.class);
                assertEquals(nonexistentQuery, resultMessage.getSearchTerm());

                // Ensure that the result is empty (no videos)
                assertTrue(resultMessage.getVideos().isEmpty(), "List is empty because item is not an array.");
            } catch (Exception e){
                fail("Item not an array, exception was not handled: " + e.getMessage());
            }
        }};
    }

    /**
     * From YouTubeService but adjusted to actor.
     */
    @Test
    public void testYouTubeServiceActorEmptyItem() throws Exception {
        new TestKit(system) {{
            // Creating a search query that is expected to return no results
            String nonexistentQuery = "nonexistentquery1234567890";

            TestProbe userActorProbe = new TestProbe(system);
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient));

            // Mocking an empty JSON response
            String emptyResponseBody = "{\"items\": []}";
            JsonNode mockJson = Json.parse(emptyResponseBody);
            when(mockResponse.asJson()).thenReturn(mockJson);

            // Setting up WSClient to return the mocked request and response
            when(mockWsClient.url(anyString())).thenReturn(mockRequest);
            when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

            youTubeServiceActor.tell(nonexistentQuery, userActorProbe.ref());

            Messages.SearchResultsMessage resultMessage =
                    userActorProbe.expectMsgClass(Messages.SearchResultsMessage.class);

            assertEquals(nonexistentQuery, resultMessage.getSearchTerm());

            // Ensure that the result is empty (no videos)
            assertTrue(resultMessage.getVideos().isEmpty(), "List is empty because there are no videos.");
        }};
    }

    /**
     * From YouTubeService but adjusted to actor.
     */
    @Test
    public void testYouTubeServiceActorNullItem() throws Exception {
        new TestKit(system) {{
            // Creating a search query that is expected to return no results
            String nonexistentQuery = "nonexistentquery1234567890";

            TestProbe userActorProbe = new TestProbe(system);
            ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient));

            // Mocking an empty JSON response
            String nullResponseBody = "{\"items\": null}";
            JsonNode mockJson = Json.parse(nullResponseBody);
            when(mockResponse.asJson()).thenReturn(mockJson);

            // Setting up WSClient to return the mocked request and response
            when(mockWsClient.url(anyString())).thenReturn(mockRequest);
            when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

            youTubeServiceActor.tell(nonexistentQuery, userActorProbe.ref());

            Messages.SearchResultsMessage resultMessage =
                    userActorProbe.expectMsgClass(Messages.SearchResultsMessage.class);

            // Ensure that the result is null
            assertTrue(resultMessage.getVideos().isEmpty(), "List is empty because item is null.");
        }};
    }

    @Test
    public void testYouTubeServiceActorErrorHandling() {
        new TestKit(system) {{
            try {
                TestProbe userActorProbe = new TestProbe(system);
                ActorRef youTubeServiceActor = system.actorOf(YouTubeServiceActor.props(mockWsClient));
                when(mockWsClient.url(anyString())).thenReturn(mockRequest);
                when(mockRequest.get()).thenReturn(CompletableFuture
                        .failedFuture(new RuntimeException("Simulated error")));

                String query = "error";
                youTubeServiceActor.tell(query, userActorProbe.ref());

                Messages.SearchResultsMessage resultMessage =
                        userActorProbe.expectMsgClass(Messages.SearchResultsMessage.class);

                assertEquals(query, resultMessage.getSearchTerm(), "Query should match even in case of error.");
                assertTrue(resultMessage.getVideos() != null, "List should be empty not null.");
                assertTrue(resultMessage.getVideos().isEmpty(), "List is empty because error occurred");
            } catch (Exception e){
                fail("Simulated Exception but was not handled: " + e.getMessage());
            }
        }};
    }
}
