package services;
import com.fasterxml.jackson.core.JsonProcessingException;
import models.Video;
import org.junit.jupiter.api.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.libs.ws.WSRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest {

  @Test
  public void testSearchVideos() throws JsonProcessingException {
    // Mocking WSClient, WSResponse, and WSRequest
    WSClient mockWsClient = mock(WSClient.class);
    WSResponse mockResponse = mock(WSResponse.class);
    WSRequest mockRequest = mock(WSRequest.class);

    // Sample JSON response simulating the YouTube API response
    String jsonResponse = "{ \"items\": [ { \"snippet\": { \"title\": \"Sample Title\", \"description\": \"Sample Description\", \"channelId\": \"channelId123\", \"resourceId\": { \"videoId\": \"videoId123\" }, \"thumbnails\": { \"default\": { \"url\": \"thumbnailUrl.jpg\" } } } } ] } }";

    // Setup mock behavior
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));
    when(mockResponse.asJson()).thenReturn(new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonResponse));

    // Instantiate the YouTubeService with the mocked WSClient
    YouTubeService youTubeService = new YouTubeService(mockWsClient);

    // Calling the method to test
    List<Video> videos = youTubeService.searchVideos("test query").toCompletableFuture().join();

    // Validating the result
    assertNotNull(videos);
    assertEquals(1, videos.size());

    Video video = videos.get(0);
    assertEquals("Sample Title", video.title());
    assertEquals("Sample Description", video.description());
    assertEquals("channelId123", video.channelId());
    assertEquals("videoId123", video.videoId());
    assertEquals("thumbnailUrl.jpg", video.thumbnailUrl());
  }
}
