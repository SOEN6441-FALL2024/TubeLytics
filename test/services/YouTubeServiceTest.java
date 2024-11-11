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
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testVideoFields() {
    Video video = new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl","channelTitle", "2024-11-06T04:41:46Z");

    assertEquals("Title", video.getTitle());
    assertEquals("Description", video.getDescription());
    assertEquals("ChannelId", video.getChannelId());
    assertEquals("VideoId", video.getVideoId());
    assertEquals("ThumbnailUrl", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
    assertEquals("2024-11-06T04:41:46Z", video.getPublishedDate());
  }
  @Test
  public void testSearchVideosWithMockResponse() throws Exception {
    // Mocking WSClient and WSResponse
    WSClient mockWsClient = mock(WSClient.class);
    WSRequest mockRequest = mock(WSRequest.class);
    WSResponse mockResponse = mock(WSResponse.class);

    // Mocking JSON response
    String responseBody = "{\"items\": [{\"snippet\": {\"title\": \"Test Video\", \"description\": \"Test Description\", \"channelId\": \"testChannel\", \"channelTitle\": \"Test Channel\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\"}, \"id\": {\"videoId\": \"videoId123\"}}]}";
    JsonNode mockJson = Json.parse(responseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    // Setting up WSClient to return mocked request and response
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    // Injecting mocks into YouTubeService
    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    // Executing the searchVideos method and validating the result
    List<Video> videos = youTubeService.searchVideos("test query");

    // Assertions
    assertEquals(1, videos.size());
    assertEquals("Test Video", videos.get(0).getTitle());
    assertEquals("Test Description", videos.get(0).getDescription());
    assertEquals("testChannel", videos.get(0).getChannelId());
    assertEquals("videoId123", videos.get(0).getVideoId());
    assertEquals("thumbnailUrl", videos.get(0).getThumbnailUrl());
    assertEquals("2024-11-06T04:41:46Z", videos.get(0).getPublishedDate());
  }

  /**
   * Tests the getChannelInfo method of YouTubeService by mocking a valid JSON response.
   * Verifies that the returned ChannelInfo object contains the expected channel details.
   *
   * @throws Exception if an error occurs during the test setup or execution
   * @author Aidassj
   */
  @Test
  public void testGetChannelInfo() throws Exception {
    // Mocking response JSON data
    String responseBody = "{\"items\": [{\"snippet\": {\"title\": \"Test Channel\", \"description\": \"Test Channel Description\"}, \"statistics\": {\"subscriberCount\": \"1000\", \"viewCount\": \"5000\", \"videoCount\": \"10\"}}]}";
    JsonNode mockJson = Json.parse(responseBody);
    mockResponse = mock(WSResponse.class);
    when(mockResponse.asJson()).thenReturn(mockJson);

    // Setting up WSClient to return mocked request and response
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    // Injecting mocks into YouTubeService
    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    // Executing the getChannelInfo method and validating the result
    ChannelInfo channelInfo = youTubeService.getChannelInfo("testChannelId");

    // Assertions
    assertNotNull(channelInfo);
    assertEquals("Test Channel", channelInfo.getName());
    assertEquals("Test Channel Description", channelInfo.getDescription());
    assertEquals(1000, channelInfo.getSubscriberCount());
    assertEquals(5000, channelInfo.getViewCount());
    assertEquals(10, channelInfo.getVideoCount());
  }
  /**
   * Tests the getLast10Videos method of YouTubeService by mocking a valid JSON response for 10 videos.
   * Verifies that the returned list contains exactly 10 Video objects with the expected details.
   *
   * @throws Exception if an error occurs during the test setup or execution
   * @author Aidassj
   */
  @Test
  public void testGetLast10Videos() throws Exception {
    // Mocking response JSON data for 10 videos
    StringBuilder responseBody = new StringBuilder("{\"items\": [");
    for (int i = 1; i <= 10; i++) {
      responseBody.append("{\"snippet\": {\"title\": \"Video ")
              .append(i)
              .append("\", \"description\": \"Description ")
              .append(i)
              .append("\", \"channelId\": \"testChannelId\", \"channelTitle\": \"ChannelTitle\", \"thumbnails\": {\"default\": {\"url\": \"thumbnailUrl")
              .append(i)
              .append("\"}}, \"publishedAt\": \"2024-11-06T04:41:46Z\"}, \"id\": {\"videoId\": \"videoId")
              .append(i)
              .append("\"}},");
    }
    responseBody.deleteCharAt(responseBody.length() - 1); // Remove last comma
    responseBody.append("]}");

    JsonNode mockJson = Json.parse(responseBody.toString());
    mockResponse = mock(WSResponse.class);
    mockRequest = mock(WSRequest.class);
    mockWsClient = mock(WSClient.class);

    when(mockResponse.asJson()).thenReturn(mockJson);

    // Setting up WSClient to return mocked request and response
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    // Injecting mocks into YouTubeService
    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    // Executing the getLast10Videos method and validating the result
    List<Video> videos = youTubeService.getLast10Videos("testChannelId");

    // Assertions
    assertNotNull(videos);
    assertEquals(10, videos.size());
    for (int i = 1; i <= 10; i++) {
      assertEquals("Video " + i, videos.get(i - 1).getTitle());
      assertEquals("Description " + i, videos.get(i - 1).getDescription());
      assertEquals("testChannelId", videos.get(i - 1).getChannelId());
      assertEquals("videoId" + i, videos.get(i - 1).getVideoId());
      assertEquals("thumbnailUrl" + i, videos.get(i - 1).getThumbnailUrl());
      assertEquals("2024-11-06T04:41:46Z", videos.get(i - 1).getPublishedDate());
    }
  }


  @Test
  public void testSearchVideos() throws Exception {
    // Mocking WSClient and WSResponse
    WSClient mockWsClient = mock(WSClient.class);
    WSRequest mockRequest = mock(WSRequest.class);
    WSResponse mockResponse = mock(WSResponse.class);

    // Creating a mock JSON response
    JsonNode mockJson = mock(JsonNode.class);
    when(mockResponse.asJson()).thenReturn(mockJson);
    when(mockJson.get("items")).thenReturn(mock(JsonNode.class));  // Mocking item list

    // Setting up WSClient to return mocked request and response
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    // Injecting mocks into YouTubeService
    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    // Executing the searchVideos method only once
    List<Video> videos = youTubeService.searchVideos("test query");

    // Verifying that the WSClient was called only once
    verify(mockWsClient, times(1)).url(contains("youtube/v3/search"));

    // Further assertions can go here
  }


  // Mock configuration for testing
  private Config mockConfig() {
    Config mockConfig = mock(Config.class);
    when(mockConfig.getString("youtube.apiKey")).thenReturn("dummy-api-key");
    return mockConfig;
  }

  @Test
  public void testSearchWithEmptyResult() throws Exception {
    // Creating a search query that is expected to return no results
    String nonexistentQuery = "nonexistentquery1234567890";

    // Mocking an empty JSON response
    String emptyResponseBody = "{\"items\": []}";
    JsonNode mockJson = Json.parse(emptyResponseBody);
    when(mockResponse.asJson()).thenReturn(mockJson);

    // Setting up WSClient to return the mocked request and response
    when(mockWsClient.url(anyString())).thenReturn(mockRequest);
    when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

    // Injecting mocks into YouTubeService
    YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

    // Executing the searchVideos method with the new input
    List<Video> videos = youTubeService.searchVideos(nonexistentQuery);

    // Ensuring that the result is empty
    assertTrue(videos.isEmpty(), "Expected search result to be empty, but it was not.");
  }
}

