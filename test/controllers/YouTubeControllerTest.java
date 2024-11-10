package controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.INTERNAL_SERVER_ERROR;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import models.ChannelInfo;
import models.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

public class YouTubeControllerTest {

  @Mock
  private YouTubeService youTubeService;

  @Mock
  private ExecutionContext ec;

  @InjectMocks
  private YouTubeController youTubeController;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(ec.prepare()).thenReturn(ec);
    youTubeController = new YouTubeController(youTubeService, ec);
  }

  @Test
  public void testSearchWithValidData() {
    Video mockVideo = new Video("Mock Title", "Mock Description", "channelId123", "videoId123", "http://mockurl.com", "Mock Channel","2024-11-06T04:41:46Z");
    List<Video> mockVideoList = Collections.singletonList(mockVideo);

    when(youTubeService.searchVideos("test")).thenReturn(mockVideoList);

    Result result = youTubeController.search("test");

    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Title"));
    assertTrue(contentAsString(result).contains("Mock Description"));
  }

  @Test
  public void testSearchWithEmptyResult() {
    when(youTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());

    Result result = youTubeController.search("empty");

    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("No results found"));
  }

  @Test
  public void testSearchWithError() {
    doThrow(new RuntimeException("API failure")).when(youTubeService).searchVideos(anyString());

    Result result = youTubeController.search("error");

    assertEquals(INTERNAL_SERVER_ERROR, result.status());
    assertTrue(contentAsString(result).contains("An error occurred while processing your request."));
  }

  @Test
  public void testSearchWithEmptyQuery() {
    Result result = youTubeController.search("");

    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  @Test
  public void testSearchWithNullQuery() {
    Result result = youTubeController.search(null);

    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  @Test
  public void testChannelProfileWithValidData() {
    ChannelInfo mockChannelInfo = new ChannelInfo("Mock Channel Name", "Mock Channel Description", 1000, 50000, 200);
    Video mockVideo = new Video("Mock Video Title", "Mock Video Description", "channelId123", "videoId123", "http://mockthumbnail.com", "Mock Channel", "2024-01-01");
    List<Video> mockVideoList = List.of(mockVideo);

    when(youTubeService.getChannelInfo("channelId123")).thenReturn(mockChannelInfo);
    when(youTubeService.getLast10Videos("channelId123")).thenReturn(mockVideoList);

    Result result = youTubeController.channelProfile("channelId123");

    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Channel Name"));
    assertTrue(contentAsString(result).contains("Mock Video Title"));
    assertTrue(contentAsString(result).contains("Mock Channel Description"));
  }

  @Test
  public void testChannelProfileWithNonExistentChannel() {
    when(youTubeService.getChannelInfo("invalidChannelId")).thenReturn(null);
    when(youTubeService.getLast10Videos("invalidChannelId")).thenReturn(Collections.emptyList());

    Result result = youTubeController.channelProfile("invalidChannelId");

    assertEquals(INTERNAL_SERVER_ERROR, result.status());
    assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
  }

  @Test
  public void testChannelProfileWithErrorInFetchingData() {
    doThrow(new RuntimeException("API failure")).when(youTubeService).getChannelInfo(anyString());
    doThrow(new RuntimeException("API failure")).when(youTubeService).getLast10Videos(anyString());

    Result result = youTubeController.channelProfile("errorChannel");

    assertEquals(INTERNAL_SERVER_ERROR, result.status());
    assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
  }

  // Additional tests for coverage

  @Test
  public void testSearchWithWhitespaceQuery() {
    Result result = youTubeController.search("   ");

    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  @Test
  public void testChannelProfileWithEmptyChannelId() {
    Result result = youTubeController.channelProfile("");

    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Invalid channel ID"));
  }

  @Test
  public void testChannelProfileWithNullChannelId() {
    Result result = youTubeController.channelProfile(null);

    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Invalid channel ID"));
  }
}
