package controllers;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
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
        Video mockVideo = new Video("Mock Title", "Mock Description", "channelId123", "videoId123", "http://mockurl.com", "Mock Channel");
        List<Video> mockVideoList = Collections.singletonList(mockVideo);

        when(youTubeService.searchVideos("test")).thenReturn(mockVideoList);

        Result result = youTubeController.search("test");

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Mock Title"));
        assertTrue(contentAsString(result).contains("Mock Description"));
        assertTrue(contentAsString(result).contains("Mock Channel"));
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
        when(youTubeService.searchVideos(anyString())).thenThrow(new RuntimeException("API failure"));

        Result result = youTubeController.search("error");

        assertEquals(500, result.status());
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
    public void testWordStatsWithNullQuery() {
        Result result = youTubeController.wordStats(null);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsString(result).contains("Please enter a search term."));
    }

    @Test
    public void testWordStatsWithSpecialCharacters() {
        Video video = new Video("Hello, World!", "Special & character test.", "channelId3", "videoId3", "http://mockurl3.com", "Channel Special");
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
        Video video1 = new Video("Java Java", "Java programming", "channelId1", "videoId1", "http://mockurl1.com", "Channel Java");
        Video video2 = new Video("Java Basics", "Basics of Java programming", "channelId2", "videoId2", "http://mockurl2.com", "Channel Basics");
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
    public void testGetVideosByTagWithValidTag() {
        Video mockVideo = new Video("Tagged Video", "Description for tag", "channelIdTag", "videoIdTag", "http://mockurlTag.com", "Tagged Channel");
        List<Video> mockVideoList = Collections.singletonList(mockVideo);

        when(youTubeService.fetchVideosByTag("tag", 10)).thenReturn(CompletableFuture.completedFuture(mockVideoList));

        CompletionStage<Result> resultStage = youTubeController.getVideosByTag("tag");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Tagged Video"));
        assertTrue(contentAsString(result).contains("Description for tag"));
        assertTrue(contentAsString(result).contains("Tagged Channel"));
    }

    @Test
    public void testGetVideosByTagWithEmptyTagResult() {
        when(youTubeService.fetchVideosByTag("emptyTag", 10)).thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        CompletionStage<Result> resultStage = youTubeController.getVideosByTag("emptyTag");
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("No results found"));
    }
}
