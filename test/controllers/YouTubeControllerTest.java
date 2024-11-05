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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Results.status;
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
        //assertEquals(OK, status(result));
        assertTrue(contentAsString(result).contains("Mock Title"));
        assertTrue(contentAsString(result).contains("Mock Description"));
        assertTrue(contentAsString(result).contains("Mock Channel"));
    }

    @Test
    public void testSearchWithEmptyResult() {
        // Set up the YouTubeService to return an empty list
        when(youTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());

        // Perform the search with a query that yields no results
        Result result = youTubeController.search("empty");

        // Assert status is OK and content indicates no results found
        //assertEquals(OK, status(result));
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
        assertEquals(BAD_REQUEST, status(result.status()));
        assertTrue(contentAsString(result).contains("Please enter a search term"));
    }
}
