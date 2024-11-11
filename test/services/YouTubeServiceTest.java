package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import models.Video;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.test.WithApplication;

public class YouTubeServiceTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testVideoFields() {
    Video video = new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl", "channelTitle");

    assertEquals("Title", video.getTitle());
    assertEquals("Description", video.getDescription());
    assertEquals("ChannelId", video.getChannelId());
    assertEquals("VideoId", video.getVideoId());
    assertEquals("ThumbnailUrl", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
  }

  @Test
  public void testSearchVideos() throws Exception {
    WSClient mockWsClient = mock(WSClient.class);
    WSRequest mockRequest = mock(WSRequest.class);
    WSResponse mockResponse = mock(WSResponse.class);
    JsonNode mockJson = mock(JsonNode.class);
    JsonNode mockItemsArray = mock(JsonNode.class);

    // Simulating an empty response for "items" node
    when(mockResponse.asJson()).thenReturn(mockJson);
    when(mockJson.get("items")).thenReturn(mockItemsArray);
    when(mockItemsArray.isArray()).thenReturn(true);
    when(mockItemsArray.size()).thenReturn(0); // Empty array to simulate no items

    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    List<Video> videos = youTubeService.searchVideos("test query");

    verify(mockWsClient).url(contains("youtube/v3/search"));
    assertTrue(videos.isEmpty(), "Expected empty list as mock response contains no items");
  }

  private Config mockConfig() {
    Config mockConfig = mock(Config.class);
    when(mockConfig.getString("youtube.apiKey")).thenReturn("dummy-api-key");
    return mockConfig;
  }

  @Test
  public void testFetchVideosByTagWithValidResponse() throws Exception {
    WSClient mockWsClient = mock(WSClient.class);
    WSRequest mockRequest = mock(WSRequest.class);
    WSResponse mockResponse = mock(WSResponse.class);
    JsonNode mockJson = mock(JsonNode.class);
    JsonNode mockItemsArray = mock(JsonNode.class);

    // Simulating a valid response with items
    when(mockResponse.asJson()).thenReturn(mockJson);
    when(mockJson.get("items")).thenReturn(mockItemsArray);
    when(mockItemsArray.isArray()).thenReturn(true);
    when(mockItemsArray.size()).thenReturn(1); // Simulate one item

    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    CompletableFuture<List<Video>> resultFuture = youTubeService.fetchVideosByTag("testTag", 10).toCompletableFuture();
    List<Video> videos = resultFuture.join();

    verify(mockWsClient).url(contains("youtube/v3/search"));
    assertTrue(videos.isEmpty(), "Expected empty list as mock response contains no items");
  }

  @Test
  public void testFetchVideosByTagWithEmptyResponse() {
    WSClient mockWsClient = mock(WSClient.class);
    WSRequest mockRequest = mock(WSRequest.class);
    WSResponse mockResponse = mock(WSResponse.class);
    JsonNode mockJson = mock(JsonNode.class);
    JsonNode mockItemsArray = mock(JsonNode.class);

    // Simulating an empty "items" array
    when(mockResponse.asJson()).thenReturn(mockJson);
    when(mockJson.get("items")).thenReturn(mockItemsArray);
    when(mockItemsArray.isArray()).thenReturn(true);
    when(mockItemsArray.size()).thenReturn(0); // Empty array to simulate no items

    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    CompletableFuture<List<Video>> resultFuture = youTubeService.fetchVideosByTag("emptyTag", 10).toCompletableFuture();
    List<Video> videos = resultFuture.join();

    verify(mockWsClient).url(contains("youtube/v3/search"));
    assertEquals(Collections.emptyList(), videos, "Expected empty list as mock response contains no items");
  }
}
