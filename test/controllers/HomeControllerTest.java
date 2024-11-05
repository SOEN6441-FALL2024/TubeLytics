package controllers;

import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import services.YouTubeService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

public class HomeControllerTest {

    @Mock
    private YouTubeService mockYouTubeService;

    @InjectMocks
    private HomeController homeController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        homeController = new HomeController(mockYouTubeService);
    }

    @Test
    public void testIndexWithQuery() {
        // Arrange
        List<Video> mockVideos = List.of(
                new Video("Title1", "Description1", "Channel1", "VideoId1", "ThumbnailUrl1", "ChannelTitle1"),
                new Video("Title2", "Description2", "Channel2", "VideoId2", "ThumbnailUrl2", "ChannelTitle2")
        );
        when(mockYouTubeService.searchVideos("test")).thenReturn(mockVideos);

        // Act
        Result result = homeController.index("test");

        // Assert
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Title1"));
        assertTrue(contentAsString(result).contains("Title2"));
    }

    @Test
    public void testIndexWithEmptyQuery() {
        // Act
        Result result = homeController.index("");

        // Assert
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("No results found")); // Assuming index page shows this text for empty results
    }

    @Test
    public void testIndexWithNullQuery() {
        // Act
        Result result = homeController.index(null);

        // Assert
        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("No results found")); // Assuming index page shows this text for empty results
    }
}
