package controllers;

import static com.gargoylesoftware.htmlunit.WebResponse.INTERNAL_SERVER_ERROR;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import actors.Messages;
import actors.SupervisorActor;
import actors.TagsActor;
import actors.WordStatsActor;
import com.typesafe.config.Config;
import models.ChannelInfo;
import models.Video;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.test.Helpers;
import services.YouTubeService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Unit test for HomeController
 * Provides tests for different methods in the HomeController class.
 *
 * @author Multiple Contributors
 */
public class HomeControllerTest {

  private LinkedHashMap<String, List<Video>> queryResults;
  private HashMap<String, LinkedHashMap<String, List<Video>>> sessionQueryMap;
  private List<Video> videos;
  private String query;
  private ActorSystem system;
  private Materializer materializer;
  private WSClient wsClient;
  private ActorRef wordStatsActor;
  private ActorRef tagsActor;

  @Mock
  private YouTubeService mockYouTubeService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private HomeController homeController;

  /**
   * Creates a unique instance of the TagsActor for testing purposes.
   *
   * @return an ActorRef pointing to the unique TagsActor
   */
  private ActorRef createUniqueTagsActor() {
    String uniqueTagsActorName = "tagsActor-" + UUID.randomUUID().toString();
    return system.actorOf(TagsActor.props(mockYouTubeService), uniqueTagsActorName);
  }

  /**
   * Initializes the testing environment before each test.
   * Sets up mock dependencies and prepares the HomeController.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    // Mock dependencies
    mockYouTubeService = mock(YouTubeService.class);
    wsClient = mock(WSClient.class); // Mock WSClient
    materializer = mock(Materializer.class);

    // Properly initialize ActorSystem
    system = ActorSystem.create("TestActorSystem");

    // Create unique TagsActor
    tagsActor = createUniqueTagsActor(); // مقداردهی قبل از سازنده HomeController

    // Initialize session-specific maps
    queryResults = new LinkedHashMap<>();
    sessionQueryMap = new HashMap<>();
    TestKit testKit = new TestKit(system);
    wordStatsActor = testKit.getRef();

    // Initialize HomeController with all required dependencies
    homeController = new HomeController(
            system,
            materializer,
            wsClient,
            mockYouTubeService,
            queryResults,
            sessionQueryMap
    );

    query = "cat";

    // Mock YouTubeService behavior
    when(mockYouTubeService.searchVideos(anyString(), eq(10)))
            .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));

    when(mockYouTubeService.getVideosByTag(anyString(), eq(10)))
            .thenReturn(CompletableFuture.completedFuture(new ArrayList<>()));
  }

  /**
   * Cleans up the testing environment after each test.
   */
  @After
  public void tearDown() {
    if (system != null) {
      TestKit.shutdownActorSystem(system);
      system = null;
    }
  }

  /**
   * Tests the WebSocket creation in the HomeController.
   */
  @Test
  public void testWs() {
    Http.Request request = mock(Http.Request.class);
    when(request.method()).thenReturn("GET");
    WebSocket ws = homeController.ws();
    assertNotNull(ws);
  }

  /**
   * Tests the WebSocket flow initialization.
   */
  @Test
  public void testWebSocketFlow() {
    new TestKit(system) {{
      Http.Request mockRequest = mock(Http.Request.class);
      WebSocket ws = homeController.ws();
      TestProbe wsProbe = new TestProbe(system);

      assertNotNull(ws);

      ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), wsClient));
      assertNotNull(supervisorActor);
    }};
  }

  /**
   * Tests the index method with a valid query.
   */
  @Test
  public void testIndexWithQuery() {
    List<Video> mockVideos = List.of(
            new Video("Title1", "Description1", "Channel1", "VideoId1", "ThumbnailUrl1", "ChannelTitle1", "2024-11-06T04:41:46Z"),
            new Video("Title2", "Description2", "Channel2", "VideoId2", "ThumbnailUrl2", "ChannelTitle2", "2024-11-06T04:41:46Z")
    );

    when(mockYouTubeService.searchVideos("test", 10))
            .thenReturn(CompletableFuture.completedFuture(mockVideos));

    Http.Request request = mock(Http.Request.class);
    Result result = homeController.index("test", request).toCompletableFuture().join();

    assertEquals(OK, result.status());
  }

  /**
   * Tests the search method with valid data.
   */
  @Test
  public void testSearchWithValidData() {
    Video mockVideo = new Video(
            "Mock Title", "Mock Description", "channelId123", "videoId123",
            "http://mockurl.com", "Mock Channel", "2024-11-06T04:41:46Z"
    );
    List<Video> mockVideoList = Collections.singletonList(mockVideo);

    when(mockYouTubeService.searchVideos("test", 10))
            .thenReturn(CompletableFuture.completedFuture(mockVideoList));

    CompletionStage<Result> resultStage = homeController.search("test");
    Result result = resultStage.toCompletableFuture().join();

    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Title"));
  }

  /**
   * Tests the search method with an empty query.
   */
  @Test
  public void testSearchWithEmptyQuery() {
    CompletionStage<Result> resultStage = homeController.search("");
    Result result = resultStage.toCompletableFuture().join();

    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  /**
   * Tests the wordStats method for frequency counts in video titles and descriptions.
   */
  @Test
  public void testWordStatsWithFrequencyCount() {
    Video video1 = new Video(
            "Java Java", "Java programming", "channelId1", "videoId1",
            "http://mockurl1.com", "Channel Java", "2024-11-06T04:41:46Z"
    );
    Video video2 = new Video(
            "Java Basics", "Basics of Java programming", "channelId2", "videoId2",
            "http://mockurl2.com", "Channel Basics", "2024-11-06T04:41:46Z"
    );

    List<Video> mockVideos = Arrays.asList(video1, video2);

    when(mockYouTubeService.searchVideos("java", 50))
            .thenReturn(CompletableFuture.completedFuture(mockVideos));

    CompletionStage<Result> resultStage = homeController.wordStats("java");
    Result result = resultStage.toCompletableFuture().join();

    assertEquals(OK, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("java"));
    assertTrue(content.contains("basics"));
    assertTrue(content.contains("programming"));
  }

  /**
   * Tests the channelProfile method with valid data.
   */
  @Test
  public void testChannelProfileWithValidData() {
    ChannelInfo mockChannelInfo = new ChannelInfo(
            "Mock Channel Name", "Mock Channel Description", 1000, 50000, 200, "channelId123"
    );
    Video mockVideo = new Video(
            "Mock Video Title", "Mock Video Description", "channelId123", "videoId123",
            "http://mockthumbnail.com", "Mock Channel", "2024-01-01"
    );
    List<Video> mockVideoList = List.of(mockVideo);

    when(mockYouTubeService.getChannelInfoAsync("channelId123"))
            .thenReturn(CompletableFuture.completedFuture(mockChannelInfo));
    when(mockYouTubeService.getLast10VideosAsync("channelId123"))
            .thenReturn(CompletableFuture.completedFuture(mockVideoList));

    Result result = homeController.channelProfile("channelId123").toCompletableFuture().join();

    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Channel Name"));
    assertTrue(contentAsString(result).contains("Mock Video Title"));
  }
}
