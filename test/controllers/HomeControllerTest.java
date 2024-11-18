package controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.contentAsString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    when(mockYouTubeService.searchVideos("test")).thenReturn(mockVideos);

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
  // Test equivalence class: eldest key is removed when array size reaches maximum
  public void testIndexEldestQueryRemoval() {
    for (int i = 0; i < 10; i++) {
      String queryNew = query + i;
      queryResults.put(queryNew, videos);
    }

    when(mockYouTubeService.searchVideos("query11")).thenReturn(videos);
    homeController.index("query11").toCompletableFuture().join();

    assertEquals(10, queryResults.size());
    assertFalse("The oldest entry - cat - should be removed", queryResults.containsKey(query));
    assertTrue("The new query exists", queryResults.containsKey("query11"));
  }

  @Test
  // Test equivalence class: query fetched result is added to map
  public void testIndexResultAddedToMap() throws ExecutionException, InterruptedException {
    when(mockYouTubeService.searchVideos(query)).thenReturn(videos);
    homeController.index(query).toCompletableFuture().join();

    assertTrue("There query should exist in Map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(1, queryResults.size());
  }

  @Test
  // Test equivalence class: existing query re-added to map
  public void testIndexExistingResultReAddedToMap() {
    // Fetching first entry
    when(mockYouTubeService.searchVideos(query)).thenReturn(videos);
    homeController.index(query).toCompletableFuture().join();

    // Fetching second entry
    when(mockYouTubeService.searchVideos("dog"))
        .thenReturn(
            List.of(
                new Video(
                    "DogVideoTitle1",
                    "DogVideoDescription1",
                    "DogVideoChannelId1",
                    "DogVideoVideoId1",
                    "DogVideoThumbnailUrl.jpg1",
                    "DogVideoChannelTitle1",
                    "2024-11-06T04:41:46Z")));
    homeController.index("dog").toCompletableFuture().join();

    // Adding first entry again
    homeController.index(query).toCompletableFuture().join();
    // Verifying "query" is only fetched once
    verify(mockYouTubeService, times(1)).searchVideos(query);

    assertTrue("There query should exist in Map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(2, queryResults.size());
  }
}
