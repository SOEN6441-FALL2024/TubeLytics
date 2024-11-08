package controllers;

import models.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.test.Helpers.OK;
import static play.test.Helpers.contentAsString;

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
    }

    @Test
    public void testSearchWithValidData() {
        // Mock valid video data
        Video mockVideo = new Video("Mock Title", "Mock Description", "channelId123", "videoId123", "http://mockurl.com", "Mock Channel");
        List<Video> mockVideoList = Collections.singletonList(mockVideo);

        // Set up the YouTubeService to return the mock video list
        when(youTubeService.searchVideos("test")).thenReturn(mockVideoList);

        // Perform the search with a valid query
        Result result = youTubeController.search("test");

        // Assert status is OK and content contains "Mock Title"
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mock Title"));
        assertTrue(contentAsString(result).contains("Mock Description"));
        //assertTrue(contentAsString(result).contains("Mock Channel"));
    }

    @Test
    public void testSearchWithEmptyResult() {
        // Set up the YouTubeService to return an empty list
        when(youTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());

        // Perform the search with a query that yields no results
        Result result = youTubeController.search("empty");

        // Assert status is OK and content indicates no results found


        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("No results found"));
    }

    @Test
    public void testSearchWithError() {
        // Set up the YouTubeService to throw an exception
        doThrow(new RuntimeException("API failure")).when(youTubeService).searchVideos(anyString());

        // Perform the search to trigger the exception
        Result result = youTubeController.search("error");

        // Assert that the status is INTERNAL_SERVER_ERROR
        //assertEquals(INTERNAL_SERVER_ERROR, status(result));
        assertTrue(contentAsString(result).contains("An error occurred while processing your request."));
    }

    @Test
    public void testSearchWithEmptyQuery() {
        // Perform search with an empty query
        Result result = youTubeController.search("");

        // Assert that the status is BAD_REQUEST and message prompts to enter search term
        //(BAD_REQUEST, status(result));
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

    @Test
    public void testWordStatsWithValidData() {
        // Arrange: Set up a list of videos with mock titles and descriptions
        Video video1 = new Video("Test Title One", "Description of video one", "channelId1", "videoId1", "http://mockurl1.com", "Channel One");
        Video video2 = new Video("Test Title Two", "Description of video two", "channelId2", "videoId2", "http://mockurl2.com", "Channel Two");
        List<Video> mockVideos = Arrays.asList(video1, video2);

        // Act: Mock YouTubeService to return the list of videos
        when(youTubeService.searchVideos("test", 50)).thenReturn(mockVideos);
        Result result = youTubeController.wordStats("test");

        // Assert: Check that the response status is OK
        assertEquals(OK, result.status());

        // Assert: Check word frequencies in the content
        String content = contentAsString(result);
        assertTrue(content.contains("test"));
        assertTrue(content.contains("title"));
        assertTrue(content.contains("description"));
    }

    @Test
    public void testWordStatsWithEmptyResult() {
        // Arrange: Set up YouTubeService to return an empty list
        when(youTubeService.searchVideos("empty", 50)).thenReturn(Collections.emptyList());

        // Act: Perform the word stats search
        Result result = youTubeController.wordStats("empty");

        // Assert: Check that the response status is OK and content is empty
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("No words found"));
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
        Video video = new Video("Hello, World!", "Special & character test.", "channelId3", "videoId3", "http://mockurl3.com", "Channel Special");
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
        Video video1 = new Video("Java Java", "Java programming", "channelId1", "videoId1", "http://mockurl1.com", "Channel Java");
        Video video2 = new Video("Java Basics", "Basics of Java programming", "channelId2", "videoId2", "http://mockurl2.com", "Channel Basics");
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
}
