package controllers;
import models.Video;
import org.junit.jupiter.api.Test;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.contentAsString;

public class YouTubeControllerTest {

    @Test
    public void testSearch() {
        // Mocking dependencies
        YouTubeService mockYouTubeService = mock(YouTubeService.class);
        ExecutionContext mockExecutionContext = mock(ExecutionContext.class);
        YouTubeController youTubeController = new YouTubeControllerBuilder().setYouTubeService(mockYouTubeService).setIgnoredEc(mockExecutionContext).createYouTubeController();

        // Creating a sample Video object
        Video sampleVideo = new Video("title", "channel", "description","videoId","thumbnailUrl");

        // Mocking the response from the YouTubeService
        when(mockYouTubeService.searchVideos("test query"))
                .thenReturn(CompletableFuture.completedFuture(List.of(sampleVideo)));

        // Calling the search method
        CompletionStage<Result> resultFuture = youTubeController.search("test query");

        // Validating the result
        Result result = resultFuture.toCompletableFuture().join(); // Wait for the result
        assertEquals(200, result.status());
        assertNotNull(contentAsString(result));
    }
}
