package controllers;

import static com.gargoylesoftware.htmlunit.WebResponse.INTERNAL_SERVER_ERROR;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import models.ChannelInfo;
import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import services.YouTubeService;

/**
 * Unit test for HomeController
 *
 * @author Deniz Dinchdonmez, Aynaz Javanivayeghan, Jessica Chen
 */
public class HomeControllerTest {
  private LinkedHashMap<String, List<Video>> queryResults;
  private List<Video> videos;
  private String query;
  @Mock private YouTubeService mockYouTubeService;

  @InjectMocks private HomeController homeController;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    mockYouTubeService = mock(YouTubeService.class);
    queryResults = new LinkedHashMap<>();
    homeController = new HomeController(mockYouTubeService, queryResults);

    query = "cat";
    // Adding mock entries into List<Video>
    videos = new ArrayList<>();
    Video video1 =
        new Video(
            "CatVideoTitle1",
            "CatVideoDescription1",
            "CatVideoChannelId1",
            "CatVideoVideoId1",
            "CatVideoThumbnailUrl.jpg1",
            "CatVideoChannelTitle1",
            "2024-11-06T04:41:46Z");
    Video video2 =
        new Video(
            "CatVideoTitle2",
            "CatVideoDescription2",
            "CatVideoChannelId2",
            "CatVideoVideoId2",
            "CatVideoThumbnailUrl.jpg2",
            "CatVideoChannelTitle2",
            "2024-11-06T04:41:46Z");
    videos.add(video1);
    videos.add(video2);
  }

  @Test
  public void testIndexWithQuery() {
    // Arrange
    List<Video> mockVideos =
        List.of(
            new Video(
                "Title1",
                "Description1",
                "Channel1",
                "VideoId1",
                "ThumbnailUrl1",
                "ChannelTitle1",
                "2024-11-06T04:41:46Z"),
            new Video(
                "Title2",
                "Description2",
                "Channel2",
                "VideoId2",
                "ThumbnailUrl2",
                "ChannelTitle2",
                "2024-11-06T04:41:46Z"));
    when(mockYouTubeService.searchVideos("test", 10))
        .thenReturn(CompletableFuture.completedFuture(mockVideos));

    // Act
    Result result = homeController.index("test").toCompletableFuture().join();

    // Assert
    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Title1"));
    assertTrue(contentAsString(result).contains("Title2"));
  }

  @Test
  public void testIndexWithEmptyQuery() {
    // Act
    Result result = homeController.index("").toCompletableFuture().join();

    // Assert
    assertEquals(OK, result.status());
    assertTrue(
        contentAsString(result)
            .contains("No results found")); // Assuming index page shows this text for empty results
  }

  @Test
  public void testIndexWithNullQuery() {
    // Act
    Result result = homeController.index(null).toCompletableFuture().join();

    // Assert
    assertEquals(OK, result.status());
    assertTrue(
        contentAsString(result)
            .contains("No results found")); // Assuming index page shows this text for empty results
  }

  @Test
  public void testIndexEldestQueryRemoval() {
    // Arrange: Populate the map with 10 entries
    for (int i = 0; i < 10; i++) {
      String queryNew = query + i;
      queryResults.put(queryNew, videos);
    }

    // Mock the YouTubeService for a new query
    when(mockYouTubeService.searchVideos("query11", 10))
        .thenReturn(CompletableFuture.completedFuture(videos));

    // Act: Add a new query to trigger eldest query removal
    homeController.index("query11").toCompletableFuture().join();

    // Assert: Verify the size remains at 10 and the eldest entry is removed
    assertEquals(10, queryResults.size());
    assertFalse("The oldest entry - cat0 - should be removed", queryResults.containsKey(query + 0));
    assertTrue("The new query exists", queryResults.containsKey("query11"));
  }

  @Test
  public void testIndexResultAddedToMap() {
    // Arrange
    when(mockYouTubeService.searchVideos(query, 10))
        .thenReturn(CompletableFuture.completedFuture(videos));

    // Act
    homeController.index(query).toCompletableFuture().join();

    // Assert
    assertTrue("The query should exist in the map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(1, queryResults.size());
  }

  @Test
  public void testIndexExistingResultReAddedToMap() {
    // Arrange: Mocking YouTubeService responses
    when(mockYouTubeService.searchVideos(query, 10))
        .thenReturn(CompletableFuture.completedFuture(videos));

    when(mockYouTubeService.searchVideos("dog", 10))
        .thenReturn(
            CompletableFuture.completedFuture(
                List.of(
                    new Video(
                        "DogVideoTitle1",
                        "DogVideoDescription1",
                        "DogVideoChannelId1",
                        "DogVideoVideoId1",
                        "DogVideoThumbnailUrl.jpg1",
                        "DogVideoChannelTitle1",
                        "2024-11-06T04:41:46Z"))));

    // Act: Fetching the same query multiple times
    homeController.index(query).toCompletableFuture().join(); // First fetch
    homeController.index("dog").toCompletableFuture().join(); // Second fetch
    homeController.index(query).toCompletableFuture().join(); // Re-fetch existing query

    // Assert: Ensure query results are as expected
    verify(mockYouTubeService, times(2)).searchVideos(query, 10); // "cat" queried twice
    verify(mockYouTubeService, times(1)).searchVideos("dog", 10); // "dog" queried once

    assertTrue("The query should exist in the map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(2, queryResults.size()); // Map size should remain 2
  }

  @Test
  public void testSearchWithValidData() {
    // Mock valid video data
    Video mockVideo =
        new Video(
            "Mock Title",
            "Mock Description",
            "channelId123",
            "videoId123",
            "http://mockurl.com",
            "Mock Channel",
            "2024-11-06T04:41:46Z");
    List<Video> mockVideoList = Collections.singletonList(mockVideo);

    // Set up the YouTubeService to return the mock video list asynchronously
    when(mockYouTubeService.searchVideos("test", 10))
        .thenReturn(CompletableFuture.completedFuture(mockVideoList));

    // Act
    CompletionStage<Result> resultStage = homeController.search("test");
    Result result = resultStage.toCompletableFuture().join();

    // Assert status is OK and content contains "Mock Title"
    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Title"));
    assertTrue(contentAsString(result).contains("Mock Description"));
  }

  @Test
  public void testSearchWithEmptyResult() {
    // Set up the YouTubeService to return an empty list
    when(mockYouTubeService.searchVideos("empty", 10))
        .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

    CompletionStage<Result> resultStage = homeController.search("empty");
    Result result = resultStage.toCompletableFuture().join();

    assertTrue(contentAsString(result).contains("No results found"));
  }

  @Test
  public void testSearchWithError() {
    // Set up the YouTubeService to throw an exception
    doReturn(CompletableFuture.failedFuture(new RuntimeException("API failure")))
        .when(mockYouTubeService)
        .searchVideos(anyString(), eq(10));

    // Act
    CompletionStage<Result> resultStage = homeController.search("error");
    Result result = resultStage.toCompletableFuture().join();

    assertTrue(
        contentAsString(result).contains("An error occurred while processing your request."));
  }

  @Test
  public void testSearchWithEmptyQuery() {
    // Perform search with an empty query
    CompletionStage<Result> resultStage = homeController.search("");
    Result result = resultStage.toCompletableFuture().join();

    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  @Test
  public void testSearchWithNullQuery() {
    // Perform search with a null query
    CompletionStage<Result> resultStage = homeController.search(null);
    Result result = resultStage.toCompletableFuture().join();

    // Assert that the status is BAD_REQUEST and message prompts to enter search term
    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Please enter a search term"));
  }

  /**
   * Tests the {@code wordStats} method with a {@code null} query.
   *
   * <p>Validates that a {@code BAD_REQUEST} status is returned and the response contains a message
   * prompting the user to enter a search term when a null query is provided.
   *
   * @throws AssertionError if the response status is not {@code BAD_REQUEST} or the expected
   *     message is not found.
   * @author Aynaz Javanivayeghan
   */
  @Test
  public void testWordStatsWithNullQuery() {
    // Act: Perform the word stats search with a null query
    CompletionStage<Result> resultStage = homeController.search(null);
    Result result = resultStage.toCompletableFuture().join();

    // Assert: Check that the response status is BAD_REQUEST
    assertEquals(BAD_REQUEST, result.status());
    assertTrue(contentAsString(result).contains("Please enter a search term."));
  }

  /**
   * Tests the {@code wordStats} method with a query containing special characters.
   *
   * <p>Ensures that words containing special characters in titles and descriptions are correctly
   * processed. Checks that the response status is {@code OK} and specific words appear in the
   * output with correct counts.
   *
   * @throws AssertionError if the response status is not {@code OK} or if expected words are not
   *     found in the output.
   * @see HomeController#wordStats(String) for details on the word statistics logic.
   * @author Aynaz Javanivayeghan
   */
  @Test
  public void testWordStatsWithSpecialCharacters() {
    // Arrange: Set up videos with special characters in titles and descriptions
    Video video =
        new Video(
            "Hello, World!",
            "Special & character test.",
            "channelId3",
            "videoId3",
            "http://mockurl3.com",
            "Channel Special",
            "2024-11-06T04:41:46Z");
    List<Video> mockVideos = Collections.singletonList(video);

    // Mock YouTubeService to return the list of videos asynchronously
    when(mockYouTubeService.searchVideos("special", 50))
        .thenReturn(CompletableFuture.completedFuture(mockVideos));

    // Act: Call the wordStats method
    CompletionStage<Result> resultStage = homeController.wordStats("special");
    Result result = resultStage.toCompletableFuture().join();

    // Assert: Check that the response status is OK and specific words are counted
    assertEquals(OK, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("hello"));
    assertTrue(content.contains("world"));
    assertTrue(content.contains("special"));
    assertTrue(content.contains("character"));
  }

  /**
   * Tests the {@code wordStats} method with a query expected to yield repetitive words.
   *
   * <p>Validates that word frequencies in video titles and descriptions are accurately counted and
   * displayed. Verifies that the response status is {@code OK} and that words and their respective
   * frequencies appear in the output.
   *
   * @throws AssertionError if the response status is not {@code OK} or expected word counts are
   *     missing.
   * @see HomeController#wordStats(String) for the sorting and counting logic applied in word
   *     statistics.
   * @throws AssertionError if expected word counts are not in the output.
   * @see HomeController for detailed logic on sorting and counting word frequencies.
   * @throws AssertionError if the response status is not {@code OK} or expected words are missing.
   * @author Aynaz Javanivayeghan
   */
  @Test
  public void testWordStatsWithFrequencyCount() {
    // Arrange: Set up videos with repetitive words
    Video video1 =
        new Video(
            "Java Java",
            "Java programming",
            "channelId1",
            "videoId1",
            "http://mockurl1.com",
            "Channel Java",
            "2024-11-06T04:41:46Z");
    Video video2 =
        new Video(
            "Java Basics",
            "Basics of Java programming",
            "channelId2",
            "videoId2",
            "http://mockurl2.com",
            "Channel Basics",
            "2024-11-06T04:41:46Z");
    List<Video> mockVideos = Arrays.asList(video1, video2);

    // Mock YouTubeService to return the list of videos asynchronously
    when(mockYouTubeService.searchVideos("java", 50))
        .thenReturn(CompletableFuture.completedFuture(mockVideos));

    // Act: Call the wordStats method
    CompletionStage<Result> resultStage = homeController.wordStats("java");
    Result result = resultStage.toCompletableFuture().join();

    // Assert: Check that the response status is OK
    assertEquals(OK, result.status());

    // Check content for word frequencies, adjust based on actual output format
    String content = contentAsString(result);

    // Adjust checks to be more flexible with HTML rendering structure if needed
    assertTrue(content.contains("java")); // Check the presence of the word 'java'
    assertTrue(content.contains("5")); // Check the frequency of 'java'
    assertTrue(content.contains("basics"));
    assertTrue(content.contains("2")); // Check for the 'basics' frequency
    assertTrue(content.contains("programming"));
    assertTrue(content.contains("2")); // Check for 'programming' frequency
  }

  @Test
  public void testSearch_NullQuery() {
    CompletionStage<Result> resultStage = homeController.search(null);
    Result result = resultStage.toCompletableFuture().join();
    assertEquals("Please enter a search term.", contentAsString(result));
  }

  @Test
  public void testSearch_EmptyQuery() {
    CompletionStage<Result> resultStage = homeController.search(" ");
    Result result = resultStage.toCompletableFuture().join();
    assertEquals("Please enter a search term.", contentAsString(result));
  }

  /**
   * Tests the {@code wordStats} method with a query that yields no videos.
   *
   * @author: Deniz Dinchdonmez
   */
  @Test
  public void testWordStats_NullQuery() {
    CompletionStage<Result> resultStage = homeController.wordStats(null);
    Result result = resultStage.toCompletableFuture().join();
    assertEquals("Please enter a search term.", contentAsString(result));
  }

  /**
   * Tests the {@code wordStats} method with a query that yields no videos.
   *
   * @author: Deniz Dinchdonmez
   */
  @Test
  public void testWordStats_EmptyQuery() {
    CompletionStage<Result> resultStage = homeController.wordStats("");
    Result result = resultStage.toCompletableFuture().join();
    assertEquals("Please enter a search term.", contentAsString(result));
  }

  /**
   * Tests the {@code wordStats} method with a query that yields no videos.
   *
   * @author: Deniz Dinchdonmez
   */
  @Test
  public void testWordStats_NoVideos() {
    // Mock the YouTube service to return an empty list for the given query
    when(mockYouTubeService.searchVideos("test-query", 50))
        .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

    // Act: Call the wordStats method with a valid query
    CompletionStage<Result> resultStage = homeController.wordStats("test-query");
    Result result = resultStage.toCompletableFuture().join();

    // Check that the response content is "No words found"
    assertEquals("No words found", contentAsString(result));
  }

  /**
   * Tests the channelProfile method with valid channel data. Verifies that the response contains
   * the expected channel and video information.
   *
   * @author Aidassj
   */
  @Test
  public void testChannelProfileWithValidData() {
    // Arrange: Mock ChannelInfo and List<Video> for a valid channel
    ChannelInfo mockChannelInfo =
        new ChannelInfo("Mock Channel Name", "Mock Channel Description", 1000, 50000, 200);
    Video mockVideo =
        new Video(
            "Mock Video Title",
            "Mock Video Description",
            "channelId123",
            "videoId123",
            "http://mockthumbnail.com",
            "Mock Channel",
            "2024-01-01");
    List<Video> mockVideoList = List.of(mockVideo);

    // Mock the service methods to return the mock data
    when(mockYouTubeService.getChannelInfo("channelId123")).thenReturn(mockChannelInfo);
    when(mockYouTubeService.getLast10Videos("channelId123")).thenReturn(mockVideoList);

    // Act: Call the channelProfile method
    Result result = homeController.channelProfile("channelId123");

    // Assert: Check status and verify content
    assertEquals(OK, result.status());
    assertTrue(contentAsString(result).contains("Mock Channel Name"));
    assertTrue(contentAsString(result).contains("Mock Video Title"));
    assertTrue(contentAsString(result).contains("Mock Channel Description"));
  }

  /**
   * Tests the channelProfile method with a non-existent channel ID. Expects an error response
   * indicating no data found.
   *
   * @author Aidassj
   */
  @Test
  public void testChannelProfileWithNonExistentChannel() {
    // Arrange: Simulate non-existent channel by returning null values
    when(mockYouTubeService.getChannelInfo("invalidChannelId")).thenReturn(null);
    when(mockYouTubeService.getLast10Videos("invalidChannelId"))
        .thenReturn(Collections.emptyList());

    // Act: Call the channelProfile method
    Result result = homeController.channelProfile("invalidChannelId");

    // Assert: Check if the response contains error or no data found message
    assertEquals(INTERNAL_SERVER_ERROR, result.status());
    assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
  }

  /**
   * Tests the channelProfile method when an exception occurs in data fetching. Expects an error
   * response with an appropriate error message.
   *
   * @author Aidassj
   */
  @Test
  public void testChannelProfileWithErrorInFetchingData() {
    // Arrange: Simulate an exception in service methods
    doThrow(new RuntimeException("API failure"))
        .when(mockYouTubeService)
        .getChannelInfo(anyString());
    doThrow(new RuntimeException("API failure"))
        .when(mockYouTubeService)
        .getLast10Videos(anyString());

    // Act: Call the channelProfile method to trigger the exception
    Result result = homeController.channelProfile("errorChannel");

    // Assert: Check if the response contains error message
    assertEquals(INTERNAL_SERVER_ERROR, result.status());
    assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
  }

  @Test
  public void testShowTagsWithValidData() {
    // Arrange: Mock a valid video with tags
    List<String> mockTags = Arrays.asList("Tag1", "Tag2", "Tag3");
    Video mockVideo =
        new Video(
            "Mock Title",
            "Mock Description",
            "channelId123",
            "videoId123",
            "http://mockurl.com",
            "Mock Channel",
            "2024-11-06T04:41:46Z");
    mockVideo.setTags(mockTags);

    // Mock the YouTubeService to return a completed future with the mock video
    when(mockYouTubeService.getVideoDetails("videoId123"))
        .thenReturn(CompletableFuture.completedFuture(mockVideo));

    // Act: Call the showTags method
    Result result = homeController.showTags("videoId123").toCompletableFuture().join();

    // Assert: Check if the response status is OK and tags are displayed
    assertEquals(OK, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("Mock Title"));
    assertTrue(content.contains("Tag1"));
    assertTrue(content.contains("Tag2"));
    assertTrue(content.contains("Tag3"));
  }

  @Test
  public void testSearchByTagWithResults() {
    // Arrange: Mock a list of videos with a specific tag
    String testTag = "testTag";
    List<Video> mockVideos =
        Arrays.asList(
            new Video(
                "Test Video 1",
                "Description 1",
                "channelId1",
                "videoId1",
                "http://thumbnail1.com",
                "Channel 1",
                "2024-11-06T04:41:46Z"),
            new Video(
                "Test Video 2",
                "Description 2",
                "channelId2",
                "videoId2",
                "http://thumbnail2.com",
                "Channel 2",
                "2024-11-06T04:41:46Z"));

    // Mock the YouTubeService to return the list of videos
    when(mockYouTubeService.searchVideosByTag(testTag))
        .thenReturn(CompletableFuture.completedFuture(mockVideos));

    // Act: Call the searchByTag method
    Result result = homeController.searchByTag(testTag).toCompletableFuture().join();

    // Assert: Check if the response status is OK and content contains video details
    assertEquals(OK, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("Videos with tag: " + testTag));
    assertTrue(content.contains("Test Video 1"));
    assertTrue(content.contains("Test Video 2"));
    assertTrue(content.contains("Description 1"));
    assertTrue(content.contains("Description 2"));
  }

  @Test
  public void testSearchByTagWithNoResults() {
    // Arrange: Mock an empty list for a tag with no videos
    String testTag = "emptyTag";
    List<Video> emptyVideos = Collections.emptyList();

    // Mock the YouTubeService to return an empty list
    when(mockYouTubeService.searchVideosByTag(testTag))
        .thenReturn(CompletableFuture.completedFuture(emptyVideos));

    // Act: Call the searchByTag method
    Result result = homeController.searchByTag(testTag).toCompletableFuture().join();

    // Assert: Check if the response status is NOT_FOUND and error message is displayed
    assertEquals(404, result.status());
    String content = contentAsString(result);
    assertTrue(content.contains("No videos found for tag: " + testTag));
  }
}
