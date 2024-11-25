package actors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.sslconfig.ssl.FakeChainedKeyStore;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.ws.WSClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserActorTest {
    private ActorSystem system;
    private WSClient mockWsClient;


    @Before
    public void setUp() {
        system = ActorSystem.create();
        mockWsClient = mock(WSClient.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests if parentActor is able to forward query to youTubeServiceActor
     * @author Jessica Chen
     */
    @Test
    public void testUserActorValidQueryForward() {
        new TestKit(system) {{
            TestProbe youTubeServiceActorProbe = new TestProbe(system);
            TestProbe wsProbe = new TestProbe(system);
            ActorRef userActor = system.actorOf(UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref()));
            String query = "cats";
            userActor.tell(query, getRef());
            youTubeServiceActorProbe.expectMsg(query);
            youTubeServiceActorProbe.lastSender().equals(userActor);
        }};
    }

    /**
     * Tests whether userActor successfully parsed the response from youtubeServiceActor and forwarded it to the client.
     * Testing with valid response.
     * @author Jessica Chen
     */
    @Test
    public void testUserActorProcessValidSearchResult() throws JsonProcessingException {
        new TestKit(system) {{
            TestProbe youTubeServiceActorProbe = new TestProbe(system);
            TestProbe wsProbe = new TestProbe(system);
            ActorRef userActor = system.actorOf(UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref()));

            String query = "sample";
            List<Video> videos = new ArrayList<>();
            videos.add(new Video(
                    "Title",
                    "Description",
                    "ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "channelTitle",
                    "2024-11-06T04:41:46Z"));

            Messages.SearchResultsMessage mockResponse =
                    new Messages.SearchResultsMessage(query, videos);
            userActor.tell(mockResponse, getRef());

            String jsonResponse = wsProbe.expectMsgClass(String.class);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonResponse);

                assertEquals(query, jsonNode.get("searchTerm").asText());
                JsonNode jsonVideos = jsonNode.get("videos");
                assertEquals(1, jsonVideos.size());
            } catch(Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }};
    }

    /**
     * Tests userActor parsing method with empty videos.
     * @author Jessica Chen
     */
    @Test
    public void testUserActorProcessSearchResultWithEmptyVideos() throws JsonProcessingException {
        new TestKit(system) {{
            TestProbe youTubeServiceActorProbe = new TestProbe(system);
            TestProbe wsProbe = new TestProbe(system);
            ActorRef userActor = system.actorOf(UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref()));

            String query = "sample";
            List<Video> emptyVideos = new ArrayList<>();

            Messages.SearchResultsMessage mockResponse =
                    new Messages.SearchResultsMessage(query, emptyVideos);
            userActor.tell(mockResponse, getRef());

            String jsonResponse = wsProbe.expectMsgClass(String.class);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonResponse);

                assertEquals(query, jsonNode.get("searchTerm").asText());
                JsonNode jsonVideos = jsonNode.get("videos");
                assertEquals(0, jsonVideos.size());
            } catch(Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }};
    }
    /**
     * Tests userActor parsing method with null videos.
     * @author Jessica Chen
     */
    @Test
    public void testUserActorProcessNullSearchResult() throws JsonProcessingException {
        new TestKit(system) {{
            TestProbe youTubeServiceActorProbe = new TestProbe(system);
            TestProbe wsProbe = new TestProbe(system);
            ActorRef userActor = system.actorOf(UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref()));

            String query = null;
            List<Video> videos = null;

            Messages.SearchResultsMessage mockResponse =
                    new Messages.SearchResultsMessage(query, videos);
            userActor.tell(mockResponse, getRef());

            String jsonResponse = wsProbe.expectMsgClass(String.class);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(jsonResponse);

                JsonNode searchTermNode = jsonNode.get("searchTerm");
                JsonNode jsonVideos = jsonNode.get("videos");

                assertTrue(searchTermNode.isEmpty());
                assertEquals(0, jsonVideos.size());
            } catch(Exception e) {
                System.err.println("Error parsing JSON: " + e.getMessage());
            }
        }};
    }
}
