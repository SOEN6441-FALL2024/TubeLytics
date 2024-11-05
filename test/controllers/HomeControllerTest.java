package controllers;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

import com.fasterxml.jackson.databind.JsonNode;
import models.SearchResult;
import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.YouTubeService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Unit test for HomeController
 *
 * @author Deniz Dinchdonmez, Aidassj, Jessica Chen
 */

public class HomeControllerTest extends WithApplication {
  private YouTubeService youTubeService;
  private HomeController homeController;
  private LinkedHashMap<String, List<Video>> queryResults;
  private List<Video> videos;
  private String query;

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testIndex() {
    // Creating a request to the root URL ("/")
    Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/");

    // Routing the request and getting the result
    Result result = route(app, request);

    // Asserting that the response status is OK (200)
    assertEquals(OK, result.status());
  }

  @Before
  public void setUp(){
    MockitoAnnotations.openMocks(this);
    // Creating a mock YouTubeService class and injecting it into HomeController
    youTubeService = mock(YouTubeService.class);
    queryResults = new LinkedHashMap<>();
    homeController = new HomeController(youTubeService, queryResults);
    query = "cat";
    // Adding mock entries into List<Video>
    videos = new ArrayList<>();
    Video video1 = new Video("CatVideoTitle1", "CatVideoDescription1", "CatVideoChannelId1",
            "CatVideoVideoId1", "CatVideoThumbnailUrl.jpg1", "CatVideoChannelTitle1");
    Video video2 = new Video("CatVideoTitle2", "CatVideoDescription2", "CatVideoChannelId2",
            "CatVideoVideoId2", "CatVideoThumbnailUrl.jpg2", "CatVideoChannelTitle2");
    videos.add(video1);
    videos.add(video2);
  }

  @Test
  // Test equivalence class: eldest key is removed when array size reaches maximum
  public void testIndexEldestQueryRemoval() {
    for (int i = 0; i < 10; i++) {
      String queryNew = query + i;
      queryResults.put(queryNew, videos);
    }

    when(youTubeService.searchVideos("query11")).thenReturn(videos);
    homeController.index("query11").toCompletableFuture().join();

    assertEquals(10, queryResults.size());
    assertFalse("The oldest entry - cat - should be removed", queryResults.containsKey(query));
    assertTrue("The new query exists", queryResults.containsKey("query11"));
  }

  @Test
  // Test equivalence class: query fetched result is added to map
  public void testIndexResultAddedToMap() throws ExecutionException, InterruptedException {
    when(youTubeService.searchVideos(query)).thenReturn(videos);
    homeController.index(query).toCompletableFuture().join();

    assertTrue("There query should exist in Map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(1, queryResults.size());
  }

  @Test
  // Test equivalence class: existing query re-added to map
  public void testIndexExistingResultReAddedToMap()  {
    // Fetching first entry
    when(youTubeService.searchVideos(query)).thenReturn(videos);
    homeController.index(query).toCompletableFuture().join();

    // Fetching second entry
    when(youTubeService.searchVideos("dog")).thenReturn(List.of(new Video("DogVideoTitle1",
            "DogVideoDescription1", "DogVideoChannelId1", "DogVideoVideoId1",
            "DogVideoThumbnailUrl.jpg1", "DogVideoChannelTitle1")));
    homeController.index("dog").toCompletableFuture().join();

    // Adding first entry again
    homeController.index(query).toCompletableFuture().join();
    // Verifying "query" is only fetched once
    verify(youTubeService, times(1)).searchVideos(query);

    assertTrue("There query should exist in Map", queryResults.containsKey(query));
    assertEquals(videos, queryResults.get(query));
    assertEquals(2, queryResults.size());
  }

  @Test
  // Test equivalence class: verify List<SearchResult> is correctly created, reversed and rendered
  public void testIndexSearchResultList() {
    when(youTubeService.searchVideos(query)).thenReturn(videos);
    Result result = homeController.index(query).toCompletableFuture().join();
    JsonNode jsonResult = play.libs.Json.parse(contentAsString(result));
  }
}
