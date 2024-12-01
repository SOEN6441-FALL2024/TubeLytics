package actors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.TestActorRef;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.ws.WSClient;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public class UserActorTest {
  private ActorSystem system;
  private WSClient mockWsClient;
  private TestProbe readActorProbe;

  @Before
  public void setUp() {
    system = ActorSystem.create();
    mockWsClient = mock(WSClient.class);
    readActorProbe = new TestProbe(system);
  }

  @After
  public void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  /**
   * Tests if parentActor is able to forward query to youTubeServiceActor
   *
   * @author Marjan khassafi, Jessica Chen
   */
  @Test
  public void testUserActorValidQueryForward() {
    new TestKit(system) {
      {
        TestProbe youTubeServiceActorProbe = new TestProbe(system);
        TestProbe wsProbe = new TestProbe(system);
        ActorRef userActor =
            system.actorOf(
                UserActor.props(
                    wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

        String query = "cats";
        userActor.tell(query, getRef());

        youTubeServiceActorProbe.expectMsg(query);
        assertEquals(userActor, youTubeServiceActorProbe.lastSender());
      }
    };
  }

  /**
   * Tests whether userActor successfully parsed the response from youtubeServiceActor and forwarded
   * it to the client. Testing with valid response.
   *
   * @author Marjan khassafi, Jessica Chen
   */
  @Test
  public void testUserActorProcessValidSearchResult() {
    new TestKit(system) {
      {
        TestProbe youTubeServiceActorProbe = new TestProbe(system);
        TestProbe wsProbe = new TestProbe(system);
        ActorRef userActor =
            system.actorOf(
                UserActor.props(
                    wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

        String query = "sample";
        List<Video> videos =
            List.of(
                new Video(
                    "Title",
                    "Description",
                    "ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z"));

        Messages.SearchResultsMessage mockResponse =
            new Messages.SearchResultsMessage(query, videos);
        userActor.tell(mockResponse, getRef());

        Messages.CalculateReadabilityMessage readabilityRequest =
                readActorProbe.expectMsgClass(Messages.CalculateReadabilityMessage.class);
        assertEquals(videos, readabilityRequest.getVideos());

        Messages.ReadabilityResultsMessage mockReadabilityResults =
                new Messages.ReadabilityResultsMessage(videos, 5.0, 80.0);
        readActorProbe.reply(mockReadabilityResults);

        String jsonResponse = wsProbe.expectMsgClass(String.class);

        try {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode jsonNode = mapper.readTree(jsonResponse);

          assertEquals(query, jsonNode.get("searchTerm").asText());
          assertEquals(1, jsonNode.get("videos").size());
        } catch (JsonProcessingException e) {
          fail("JSON parsing failed: " + e.getMessage());
        }
      }
    };
  }

  /**
   * Tests userActor parsing method with null videos.
   *
   * @author Marjan Khassafi, Jessica Chen
   */
  @Test
  public void testUserActorProcessNullSearchResult() {
    new TestKit(system) {{
      TestProbe wsProbe = new TestProbe(system);
      TestProbe youTubeServiceActorProbe = new TestProbe(system);
      TestProbe readActorProbe = new TestProbe(system);

      ActorRef userActor =
              system.actorOf(
                      UserActor.props(
                              wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

      String query = null;
      List<Video> videos = null;

      Messages.SearchResultsMessage mockResponse =
              new Messages.SearchResultsMessage(query, videos);
      userActor.tell(mockResponse, getRef());

      // Mock readability response
      Messages.CalculateReadabilityMessage readabilityRequest =
              readActorProbe.expectMsgClass(Messages.CalculateReadabilityMessage.class);

      Messages.ReadabilityResultsMessage mockReadabilityResults =
              new Messages.ReadabilityResultsMessage(Collections.emptyList(), 0.0, 0.0);
      readActorProbe.reply(mockReadabilityResults);

      String jsonResponse = wsProbe.expectMsgClass(String.class);

      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonResponse);

        // Validate searchTerm
        assertTrue(jsonNode.has("searchTerm"));
        assertEquals("Unknown", jsonNode.get("searchTerm").asText());

        // Validate videos
        assertTrue(jsonNode.has("videos") && jsonNode.get("videos").isArray());
        assertEquals(0, jsonNode.get("videos").size());
      } catch (JsonProcessingException e) {
        fail("JSON parsing failed: " + e.getMessage());
      }
    }};
  }


  @Test
  public void testRepeatedQueryHandling() {
    new TestKit(system) {
      {
        TestProbe wsProbe = new TestProbe(system);
        TestProbe youTubeServiceActorProbe = new TestProbe(system);
        ActorRef userActor =
            system.actorOf(
                UserActor.props(
                    wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

        String query = "repeated-query";

        // Send the same query multiple times
        userActor.tell(query, getRef());
        userActor.tell(query, getRef());

        // Validate the query is forwarded only once
        youTubeServiceActorProbe.expectMsg(query);
        youTubeServiceActorProbe.expectNoMessage(Duration.create(500, "millis"));
      }
    };
  }

  @Test
  public void testUnhandledMessage() {
    new TestKit(system) {
      {
        TestProbe wsProbe = new TestProbe(system);
        TestProbe youTubeServiceActorProbe = new TestProbe(system);
        ActorRef userActor =
            system.actorOf(
                UserActor.props(
                    wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

        // Send an unsupported message type
        userActor.tell(42, getRef());

        // Validate no response is sent to WebSocket
        wsProbe.expectNoMessage(Duration.create(500, "millis"));
      }
    };
  }

  @Test
  public void testProcessReceivedResults() {
    new TestKit(system) {{
      TestProbe wsProbe = new TestProbe(system);
      TestProbe youTubeServiceActorProbe = new TestProbe(system);
      TestProbe readActorProbe = new TestProbe(system);

      ActorRef userActor =
              system.actorOf(
                      UserActor.props(
                              wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

      // Case 1: Test with null videos in response
      Messages.SearchResultsMessage nullResponse =
              new Messages.SearchResultsMessage("null-query", null);
      userActor.tell(nullResponse, getRef());

      // Mock readability results
      readActorProbe.expectMsgClass(Messages.CalculateReadabilityMessage.class);
      readActorProbe.reply(new Messages.ReadabilityResultsMessage(Collections.emptyList(), 0.0, 0.0));

      String nullJsonResponse = wsProbe.expectMsgClass(FiniteDuration.create(5, "seconds"), String.class);
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nullJsonNode = mapper.readTree(nullJsonResponse);

        assertEquals("null-query", nullJsonNode.get("searchTerm").asText());
        assertTrue(nullJsonNode.get("videos").isArray());
        assertEquals(0, nullJsonNode.get("videos").size());
      } catch (JsonProcessingException e) {
        fail("JSON parsing failed for nullResponse: " + e.getMessage());
      }

      // Case 2: Test adding more than 10 unique videos
      List<Video> videos = new ArrayList<>();
      for (int i = 0; i < 12; i++) {
        videos.add(
                new Video(
                        "Title" + i,
                        "Description" + i,
                        "ChannelId" + i,
                        "VideoId" + i,
                        "ThumbnailUrl" + i,
                        "ChannelTitle" + i,
                        "2024-11-06T04:41:46Z"));
      }
      Messages.SearchResultsMessage largeResponse =
              new Messages.SearchResultsMessage("test-query", videos);
      userActor.tell(largeResponse, getRef());

      readActorProbe.expectMsgClass(Messages.CalculateReadabilityMessage.class);
      readActorProbe.reply(new Messages.ReadabilityResultsMessage(videos, 5.0, 80.0));

      String largeJsonResponse = wsProbe.expectMsgClass(FiniteDuration.create(5, "seconds"), String.class);
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(largeJsonResponse);

        assertEquals("test-query", jsonNode.get("searchTerm").asText());
        assertEquals(10, jsonNode.get("videos").size());

        // Validate the 10 newest videos
        for (int i = 0; i < 10; i++) {
          int expectedIndex = 11 - i; // Most recent videos are at the start
          JsonNode videoNode = jsonNode.get("videos").get(i);
          assertEquals("Title" + expectedIndex, videoNode.get("title").asText());
        }
      } catch (JsonProcessingException e) {
        fail("JSON parsing failed for largeResponse: " + e.getMessage());
      }
    }};
  }

  @Test
  public void testSendResultsToClientJsonProcessingException() throws JsonProcessingException {
    new TestKit(system) {{
      TestProbe wsProbe = new TestProbe(system);
      TestProbe youTubeServiceActorProbe = new TestProbe(system);
      TestProbe readActorProbe = new TestProbe(system);

      // Create a mock for ObjectMapper
      ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);

      // Mock behavior to return a valid ObjectNode for createObjectNode
      ObjectNode mockObjectNode = new ObjectMapper().createObjectNode(); // Create a real ObjectNode
      Mockito.when(mockObjectMapper.createObjectNode()).thenReturn(mockObjectNode);

      // Mock behavior to throw JsonProcessingException for writeValueAsString
      try {
        Mockito.when(mockObjectMapper.writeValueAsString(Mockito.any()))
                .thenThrow(new JsonProcessingException("Mocked JSON processing error") {});
      } catch (JsonProcessingException e) {
        fail("Mock setup failed: " + e.getMessage());
      }

      // Create the UserActor
      ActorRef userActor =
              system.actorOf(
                      UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

      // Send a mock ReadabilityResultsMessage to trigger the method
      List<Video> videos = List.of(
              new Video(
                      "Title",
                      "Description",
                      "ChannelId",
                      "VideoId",
                      "ThumbnailUrl",
                      "ChannelTitle",
                      "2024-11-06T04:41:46Z")
      );
      Messages.ReadabilityResultsMessage mockReadabilityResults =
              new Messages.ReadabilityResultsMessage(videos, 5.0, 80.0);

      // Inject the mocked ObjectMapper into UserActor
      TestActorRef<UserActor> testUserActor = TestActorRef.create(system,
              UserActor.props(wsProbe.ref(), youTubeServiceActorProbe.ref(), readActorProbe.ref()));

      testUserActor.underlyingActor().setObjectMapper(mockObjectMapper);

      // Send the message
      testUserActor.tell(mockReadabilityResults, getRef());

      // Verify that the error log is triggered
      Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(Mockito.any());

      // Assert no message is sent to WebSocket
      wsProbe.expectNoMessage();
    }};
  }


}
