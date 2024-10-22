package controllers;

import models.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

public class YouTubeControllerTest extends WithApplication {

    private YouTubeService mockYouTubeService;
    private ExecutionContext mockExecutionContext;
    private YouTubeController youTubeController;

    @Override
    protected Application provideApplication() {
        // Building the application using Guice
        return new GuiceApplicationBuilder().build();
    }

    @BeforeEach
    public void setUp() {
        this.mockYouTubeService = mock(YouTubeService.class);
        mockExecutionContext = mock(ExecutionContext.class);
        youTubeController = new YouTubeController(mockYouTubeService, mockExecutionContext);
    }

    @Test
    public void testSearchSuccess() {
        // Creating a sample Video object
        Video sampleVideo = new Video("title", "description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        this.mockYouTubeService = mock(YouTubeService.class);

        // Mocking the response from the YouTubeService
        when(mockYouTubeService.searchVideos("testquery"))
                .thenReturn((List.of(sampleVideo)));

        // Calling the search method
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/search?query=testquery");

        // Routing the request and getting the result
        Result result = route(app, request);

        assertEquals(200, result.status());
        assertTrue(contentAsString(result).contains("title"));  // Validate content
    }

    @Test
    public void testSearchNoResults() {
        // Mocking the response from the YouTubeService to return an empty list
        this.mockYouTubeService = mock(YouTubeService.class);
        when(mockYouTubeService.searchVideos("testquery"))
                .thenReturn(Collections.emptyList());

        // Calling the search method
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/search?query=testquery");

        // Routing the request and getting the result
        Result result = route(app, request);

        // Validating the result
        assertEquals(200, result.status());// Assuming the view renders a message
    }

    @Test
    public void testSearchNullQuery() {
        // Mocking the response from the YouTubeService for null input
        this.mockYouTubeService = mock(YouTubeService.class);
        when(mockYouTubeService.searchVideos(null))
                .thenReturn(Collections.emptyList());

        // Calling the search method with a null query
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/search");

        // Routing the request and getting the result
        Result result = route(app, request);

        assertEquals(400, result.status());
    }

    @Test
    public void testVideoEquality() {
        Video video1 = new Video("title", "description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("title", "description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        // Check equality
        assertEquals(video1, video2);

        // Check hashCode
        assertEquals(video1.hashCode(), video2.hashCode());
    }

    @Test
    public void testVideoInequality() {
        Video video1 = new Video("title", "description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("title2", "description2", "channelId124", "videoId124", "thumbnailUrl2.jpg");

        // Check inequality
        assertNotEquals(video1, video2);
    }
}
