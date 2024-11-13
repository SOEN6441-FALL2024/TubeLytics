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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                        "Mock Channel",
                        "2024-11-06T04:41:46Z",
                        Arrays.asList("tag1", "tag2")); // اضافه کردن tags
        List<Video> mockVideoList = Collections.singletonList(mockVideo);

        // Set up the YouTubeService to return the mock video list
        when(youTubeService.searchVideos("test")).thenReturn(mockVideoList);

        // Perform the search with a valid query
        Result result = youTubeController.search("test");

        // Assert status is OK and content contains "Mock Title"
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mock Title"));
        assertTrue(contentAsString(result).contains("Mock Description"));
    }

    @Test
    public void testSearchWithEmptyResult() {
        when(youTubeService.searchVideos("empty")).thenReturn(Collections.emptyList());
        Result result = youTubeController.search("empty");
        assertTrue(contentAsString(result).contains("No results found"));
    }

    @Test
    public void testSearchWithError() {
        doThrow(new RuntimeException("API failure")).when(youTubeService).searchVideos(anyString());
        Result result = youTubeController.search("error");
        assertTrue(contentAsString(result).contains("An error occurred while processing your request."));
    }

    @Test
    public void testSearchWithEmptyQuery() {
        Result result = youTubeController.search("");
        assertTrue(contentAsString(result).contains("Please enter a search term"));
    }

    @Test
    public void testSearchWithNullQuery() {
        Result result = youTubeController.search(null);
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a search term"));
    }

    @Test
    public void testWordStatsWithNullQuery() {
        Result result = youTubeController.wordStats(null);
        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a search term."));
    }

    @Test
    public void testWordStatsWithSpecialCharacters() {
        Video video = new Video("Hello, World!", "Special & character test.", "channelId3", "videoId3", "http://mockurl3.com", "Channel Special","2024-11-06T04:41:46Z", Arrays.asList("tag1")); // اضافه کردن tags
        List<Video> mockVideos = Collections.singletonList(video);

        when(youTubeService.searchVideos("special", 50)).thenReturn(mockVideos);
        Result result = youTubeController.wordStats("special");

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("hello"));
        assertTrue(content.contains("world"));
        assertTrue(content.contains("special"));
        assertTrue(content.contains("character"));
    }

    @Test
    public void testWordStatsWithFrequencyCount() {
        Video video1 = new Video("Java Java", "Java programming", "channelId1", "videoId1", "http://mockurl1.com", "Channel Java","2024-11-06T04:41:46Z", Arrays.asList("tag2")); // اضافه کردن tags
        Video video2 = new Video("Java Basics", "Basics of Java programming", "channelId2", "videoId2", "http://mockurl2.com", "Channel Basics","2024-11-06T04:41:46Z", Arrays.asList("tag3")); // اضافه کردن tags
        List<Video> mockVideos = Arrays.asList(video1, video2);

        when(youTubeService.searchVideos("java", 50)).thenReturn(mockVideos);
        Result result = youTubeController.wordStats("java");

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("java"));
        assertTrue(content.contains("5"));
        assertTrue(content.contains("basics"));
        assertTrue(content.contains("2"));
        assertTrue(content.contains("programming"));
        assertTrue(content.contains("2"));
    }

    @Test
    public void testChannelProfileWithValidData() {
        ChannelInfo mockChannelInfo = new ChannelInfo("Mock Channel Name", "Mock Channel Description", 1000, 50000, 200);
        Video mockVideo = new Video("Mock Video Title", "Mock Video Description", "channelId123", "videoId123", "http://mockthumbnail.com", "Mock Channel", "2024-01-01", Arrays.asList("tag1", "tag2")); // اضافه کردن tags
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
}
