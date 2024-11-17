package controllers;

import models.ChannelInfo;
import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;
import java.util.ArrayList;
import java.util.List;



import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;


import static com.gargoylesoftware.htmlunit.WebResponse.INTERNAL_SERVER_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.OK;
import static play.test.Helpers.contentAsString;
public class YouTubeControllerTest {

    @Mock private YouTubeService youTubeService;
    @Mock private ExecutionContext ec;

    @InjectMocks private YouTubeController youTubeController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
                        "Mock Channel","2024-11-06T04:41:46Z");
        List<Video> mockVideoList = Collections.singletonList(mockVideo);

        // Set up the YouTubeService to return the mock video list
        when(youTubeService.searchVideos("test")).thenReturn(mockVideoList);

        // Perform the search with a valid query
        Result result = youTubeController.search("test");

        // Assert status is OK and content contains "Mock Title"
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mock Title"));
        assertTrue(contentAsString(result).contains("Mock Description"));
        // assertTrue(contentAsString(result).contains("Mock Channel"));
    }

    @Test
    public void testSearchWithEmptyResult() {
        // Set up the YouTubeService to return an empty list
        when(youTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());

        // Perform the search with a query that yields no results
        Result result = youTubeController.search("empty");

        // Assert status is OK and content indicates no results found
        // assertEquals(OK, status(result));
        assertTrue(contentAsString(result).contains("No results found"));
    }

    @Test
    public void testSearchWithError() {
        // Set up the YouTubeService to throw an exception
        doThrow(new RuntimeException("API failure")).when(youTubeService).searchVideos(anyString());

        // Perform the search to trigger the exception
        Result result = youTubeController.search("error");

        // Assert that the status is INTERNAL_SERVER_ERROR
        // assertEquals(INTERNAL_SERVER_ERROR, status(result));
        assertTrue(
                contentAsString(result).contains("An error occurred while processing your request."));
    }

    @Test
    public void testSearchWithEmptyQuery() {
        // Perform search with an empty query
        Result result = youTubeController.search("");

        // Assert that the status is BAD_REQUEST and message prompts to enter search term
        // (BAD_REQUEST, status(result));
        assertTrue(contentAsString(result).contains("Please enter a search term"));
    }

    @Test
    public void testSearchWithNullQuery() {
        // Perform search with a null query
        Result result = youTubeController.search(null);

        // Assert that the status is BAD_REQUEST and message prompts to enter search term
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a search term"));
    }

    /**
     * Tests the {@code wordStats} method with a {@code null} query.
     *
     * <p>Validates that a {@code BAD_REQUEST} status is returned and the response contains a message prompting
     * the user to enter a search term when a null query is provided.</p>
     *
     * @throws AssertionError if the response status is not {@code BAD_REQUEST} or the expected message is not found.
     * @author Aynaz Javanivayeghan
     */
    @Test
    public void testWordStatsWithNullQuery() {
        // Act: Perform the word stats search with a null query
        Result result = youTubeController.wordStats(null);

        // Assert: Check that the response status is BAD_REQUEST
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a search term."));
    }
    /**
     * Tests the {@code wordStats} method with a query containing special characters.
     *
     * <p>Ensures that words containing special characters in titles and descriptions are correctly processed.
     * Checks that the response status is {@code OK} and specific words appear in the output with correct counts.</p>
     *
     * @throws AssertionError if the response status is not {@code OK} or if expected words are not found in the output.
     * @see YouTubeController#wordStats(String) for details on the word statistics logic.
     * @author Aynaz Javanivayeghan
     */
    @Test
    public void testWordStatsWithSpecialCharacters() {
        // Arrange: Set up videos with special characters in titles and descriptions
        Video video = new Video("Hello, World!", "Special & character test.", "channelId3", "videoId3", "http://mockurl3.com", "Channel Special","2024-11-06T04:41:46Z");
        List<Video> mockVideos = Collections.singletonList(video);

        // Act: Mock YouTubeService to return the list of videos
        when(youTubeService.searchVideos("special", 50)).thenReturn(mockVideos);
        Result result = youTubeController.wordStats("special");

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
     * <p>Validates that word frequencies in video titles and descriptions are accurately counted
     * and displayed. Verifies that the response status is {@code OK} and that words and their
     * respective frequencies appear in the output.</p>
     *
     * @throws AssertionError if the response status is not {@code OK} or expected word counts are missing.
     * @see YouTubeController#wordStats(String) for the sorting and counting logic applied in word statistics.
     * @throws AssertionError if expected word counts are not in the output.
     * @see YouTubeController for detailed logic on sorting and counting word frequencies.
     * @throws AssertionError if the response status is not {@code OK} or expected words are missing.
     * @author Aynaz Javanivayeghan


     */
    @Test
    public void testWordStatsWithFrequencyCount() {
        // Arrange: Set up videos with repetitive words
        Video video1 = new Video("Java Java", "Java programming", "channelId1", "videoId1", "http://mockurl1.com", "Channel Java","2024-11-06T04:41:46Z");
        Video video2 = new Video("Java Basics", "Basics of Java programming", "channelId2", "videoId2", "http://mockurl2.com", "Channel Basics","2024-11-06T04:41:46Z");
        List<Video> mockVideos = Arrays.asList(video1, video2);

        // Act: Mock YouTubeService to return the list of videos
        when(youTubeService.searchVideos("java", 50)).thenReturn(mockVideos);
        Result result = youTubeController.wordStats("java");

        // Assert: Check that the response status is OK
        assertEquals(OK, result.status());

        // Check content for word frequencies, adjust based on actual output format
        String content = contentAsString(result);

        // Adjust checks to be more flexible with HTML rendering structure if needed
        assertTrue(content.contains("java")); // Check the presence of the word 'java'
        assertTrue(content.contains("5"));    // Check the frequency of 'java'
        assertTrue(content.contains("basics"));
        assertTrue(content.contains("2"));    // Check for the 'basics' frequency
        assertTrue(content.contains("programming"));
        assertTrue(content.contains("2"));    // Check for 'programming' frequency
    }
    /**
     * Tests the channelProfile method with valid channel data.
     * Verifies that the response contains the expected channel and video information.
     * @author Aidassj
     */

    @Test
    public void testChannelProfileWithValidData() {
        // Arrange: Mock ChannelInfo and List<Video> for a valid channel
        ChannelInfo mockChannelInfo = new ChannelInfo("Mock Channel Name", "Mock Channel Description", 1000, 50000, 200);
        Video mockVideo = new Video("Mock Video Title", "Mock Video Description", "channelId123", "videoId123", "http://mockthumbnail.com", "Mock Channel", "2024-01-01");
        List<Video> mockVideoList = List.of(mockVideo);

        // Mock the service methods to return the mock data
        when(youTubeService.getChannelInfo("channelId123")).thenReturn(mockChannelInfo);
        when(youTubeService.getLast10Videos("channelId123")).thenReturn(mockVideoList);

        // Act: Call the channelProfile method
        Result result = youTubeController.channelProfile("channelId123");

        // Assert: Check status and verify content
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mock Channel Name"));
        assertTrue(contentAsString(result).contains("Mock Video Title"));
        assertTrue(contentAsString(result).contains("Mock Channel Description"));
    }
    /**
     * Tests the channelProfile method with a non-existent channel ID.
     * Expects an error response indicating no data found.
     * @author Aidassj
     */
    @Test
    public void testChannelProfileWithNonExistentChannel() {
        // Arrange: Simulate non-existent channel by returning null values
        when(youTubeService.getChannelInfo("invalidChannelId")).thenReturn(null);
        when(youTubeService.getLast10Videos("invalidChannelId")).thenReturn(Collections.emptyList());

        // Act: Call the channelProfile method
        Result result = youTubeController.channelProfile("invalidChannelId");

        // Assert: Check if the response contains error or no data found message
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
    }
    /**
     * Tests the channelProfile method when an exception occurs in data fetching.
     * Expects an error response with an appropriate error message.
     * @author Aidassj
     */
    @Test
    public void testChannelProfileWithErrorInFetchingData() {
        // Arrange: Simulate an exception in service methods
        doThrow(new RuntimeException("API failure")).when(youTubeService).getChannelInfo(anyString());
        doThrow(new RuntimeException("API failure")).when(youTubeService).getLast10Videos(anyString());

        // Act: Call the channelProfile method to trigger the exception
        Result result = youTubeController.channelProfile("errorChannel");

        // Assert: Check if the response contains error message
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        assertTrue(contentAsString(result).contains("An error occurred while fetching channel data."));
    }
    @Test
    public void testShowTagsWithValidData() {
        // Arrange: Mock a valid video with tags
        List<String> mockTags = Arrays.asList("Tag1", "Tag2", "Tag3");
        Video mockVideo = new Video(
                "Mock Title",
                "Mock Description",
                "channelId123",
                "videoId123",
                "http://mockurl.com",
                "Mock Channel",
                "2024-11-06T04:41:46Z"
        );
        mockVideo.setTags(mockTags);

        // Mock the YouTubeService to return a completed future with the mock video
        when(youTubeService.getVideoDetails("videoId123"))
                .thenReturn(CompletableFuture.completedFuture(mockVideo));

        // Act: Call the showTags method
        Result result = youTubeController.showTags("videoId123").toCompletableFuture().join();


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
        List<Video> mockVideos = Arrays.asList(
                new Video("Test Video 1", "Description 1", "channelId1", "videoId1", "http://thumbnail1.com", "Channel 1", "2024-11-06T04:41:46Z"),
                new Video("Test Video 2", "Description 2", "channelId2", "videoId2", "http://thumbnail2.com", "Channel 2", "2024-11-06T04:41:46Z")
        );

        // Mock the YouTubeService to return the list of videos
        when(youTubeService.searchVideosByTag(testTag))
                .thenReturn(CompletableFuture.completedFuture(mockVideos));

        // Act: Call the searchByTag method
        Result result = youTubeController.searchByTag(testTag).toCompletableFuture().join();

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
        when(youTubeService.searchVideosByTag(testTag))
                .thenReturn(CompletableFuture.completedFuture(emptyVideos));

        // Act: Call the searchByTag method
        Result result = youTubeController.searchByTag(testTag).toCompletableFuture().join();

        // Assert: Check if the response status is NOT_FOUND and error message is displayed
        assertEquals(404, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("No videos found for tag: " + testTag));
    }

}
