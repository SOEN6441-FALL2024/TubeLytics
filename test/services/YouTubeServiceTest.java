package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.ChannelInfo;
import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.test.WithApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest extends WithApplication {

  private WSClient mockWsClient;
  private WSRequest mockRequest;
  private WSResponse mockResponse;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockRequest = mock(WSRequest.class);
    mockWsClient = mock(WSClient.class);
    mockResponse = mock(WSResponse.class);

    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));
  }

  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testVideoFields() {
    Video video = new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl", "channelTitle", "2024-11-06T04:41:46Z", List.of("tag1", "tag2"));

    assertEquals("Title", video.getTitle());
    assertEquals("Description", video.getDescription());
    assertEquals("ChannelId", video.getChannelId());
    assertEquals("VideoId", video.getVideoId());
    assertEquals("ThumbnailUrl", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
    assertEquals("2024-11-06T04:41:46Z", video.getPublishedDate());
    assertEquals(List.of("tag1", "tag2"), video.getTags());
  }

  @Test
  public void testSearchVideosWithMockResponse() throws Exception {
    String responseBody = "{\"items\": [{\"snippet\": {\"title\": \"Test Video\", \"description\": \"Test Description\", \"channelId\": \"testChannel\", \"channelTitle\": \"Test Channel\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\", \"tags\": [\"tag1\", \"tag2\"]}, \"id\": {\"videoId\": \"videoId123\"}}]}";
    JsonNode mockJson = Json.parse(responseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    List<Video> videos = youTubeService.searchVideos("test query");

    assertEquals(1, videos.size());
    assertEquals("Test Video", videos.get(0).getTitle());
    assertEquals("Test Description", videos.get(0).getDescription());
    assertEquals("testChannel", videos.get(0).getChannelId());
    assertEquals("videoId123", videos.get(0).getVideoId());
    assertEquals("thumbnailUrl", videos.get(0).getThumbnailUrl());
    assertEquals("2024-11-06T04:41:46Z", videos.get(0).getPublishedDate());
    assertEquals(List.of("tag1", "tag2"), videos.get(0).getTags());
  }

  @Test
  public void testGetChannelInfo() throws Exception {
    String responseBody = "{\"items\": [{\"snippet\": {\"title\": \"Test Channel\", \"description\": \"Test Channel Description\"}, \"statistics\": {\"subscriberCount\": \"1000\", \"viewCount\": \"5000\", \"videoCount\": \"10\"}}]}";
    JsonNode mockJson = Json.parse(responseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    ChannelInfo channelInfo = youTubeService.getChannelInfo("testChannelId");

    assertNotNull(channelInfo);
    assertEquals("Test Channel", channelInfo.getName());
    assertEquals("Test Channel Description", channelInfo.getDescription());
    assertEquals(1000, channelInfo.getSubscriberCount());
    assertEquals(5000, channelInfo.getViewCount());
    assertEquals(10, channelInfo.getVideoCount());
  }

  @Test
  public void testGetLast10Videos() throws Exception {
    StringBuilder responseBody = new StringBuilder("{\"items\": [");
    for (int i = 1; i <= 10; i++) {
      responseBody.append("{\"snippet\": {\"title\": \"Video ")
              .append(i)
              .append("\", \"description\": \"Description ")
              .append(i)
              .append("\", \"channelId\": \"testChannelId\", \"channelTitle\": \"ChannelTitle\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl")
              .append(i)
              .append("\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\", \"tags\": [\"tag1\", \"tag2\"]}, \"id\": {\"videoId\": \"videoId")
              .append(i)
              .append("\"}},");
    }
    responseBody.deleteCharAt(responseBody.length() - 1);
    responseBody.append("]}");

    JsonNode mockJson = Json.parse(responseBody.toString());
    when(mockResponse.asJson()).thenReturn(mockJson);

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    List<Video> videos = youTubeService.getLast10Videos("testChannelId");

    assertNotNull(videos);
    assertEquals(10, videos.size());
    for (int i = 1; i <= 10; i++) {
      assertEquals("Video " + i, videos.get(i - 1).getTitle());
      assertEquals("Description " + i, videos.get(i - 1).getDescription());
      assertEquals("testChannelId", videos.get(i - 1).getChannelId());
      assertEquals("videoId" + i, videos.get(i - 1).getVideoId());
      assertEquals("thumbnailUrl" + i, videos.get(i - 1).getThumbnailUrl());
      assertEquals("2024-11-06T04:41:46Z", videos.get(i - 1).getPublishedDate());
      assertEquals(List.of("tag1", "tag2"), videos.get(i - 1).getTags());
    }
  }

  @Test
  public void testGetVideosByTag() throws Exception {
    String responseBody = "{\"items\": [{\"snippet\": {\"title\": \"Tagged Video\", \"description\": \"Video with tag\", \"channelId\": \"channel123\", \"channelTitle\": \"ChannelTitle\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl.jpg\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\", \"tags\": [\"sampleTag\"]}, \"id\": {\"videoId\": \"video123\"}}]}";
    JsonNode mockJson = Json.parse(responseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    List<Video> videos = youTubeService.getVideosByTag("sampleTag").toCompletableFuture().join();

    assertEquals(1, videos.size());
    assertEquals("Tagged Video", videos.get(0).getTitle());
    assertEquals("Video with tag", videos.get(0).getDescription());
    assertEquals("channel123", videos.get(0).getChannelId());
    assertEquals("video123", videos.get(0).getVideoId());
    assertEquals("thumbnailUrl.jpg", videos.get(0).getThumbnailUrl());
    assertEquals("2024-11-06T04:41:46Z", videos.get(0).getPublishedDate());
    assertEquals(List.of("sampleTag"), videos.get(0).getTags());
  }

  private Config mockConfig() {
    Config mockConfig = mock(Config.class);
    when(mockConfig.getString("youtube.apiKey")).thenReturn("dummy-api-key");
    return mockConfig;
  }

  @Test
  public void testSearchWithEmptyResult() throws Exception {
    String nonexistentQuery = "nonexistentquery1234567890";
    String emptyResponseBody = "{\"items\": []}";
    JsonNode mockJson = Json.parse(emptyResponseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    List<Video> videos = youTubeService.searchVideos(nonexistentQuery);

    assertTrue(videos.isEmpty(), "Expected search result to be empty, but it was not.");
  }
}
