package controllers;

import models.SearchResult;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.YouTubeService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class HomeController extends Controller {

  private final YouTubeService youTubeService;
  private final LinkedHashMap<String, List<Video>>  multipleQueryResult = new LinkedHashMap<>();


  @Inject
  public HomeController(YouTubeService youTubeService) {
    this.youTubeService = youTubeService;
  }

  public CompletionStage<Result> index(String query) {
    return CompletableFuture.supplyAsync( () -> {
      // If query is new
      if (!multipleQueryResult.containsKey(query)){
        // If size of map is at 10 already, delete the oldest entry
        if(multipleQueryResult.size() == 10) {
          String eldestKey = multipleQueryResult.keySet().iterator().next();
          multipleQueryResult.remove(eldestKey);
        }
        // Fetch videos or return an empty list if query is null
        List<models.Video> videos = (query == null || query.isEmpty()) ?
                List.of() :
                youTubeService.searchVideos(query);

        // Add query entry to Map
        multipleQueryResult.put(query, videos);
      } else {
        List<Video> videos = multipleQueryResult.get(query);
        multipleQueryResult.remove(query);
        multipleQueryResult.put(query, videos);
      }
      return multipleQueryResult;
    }).thenApply(multipleQueryResult -> {
      // Create SearchResult objects to hold query and List<Video> based on Map
      List<SearchResult> searchResults = multipleQueryResult.entrySet().stream()
              .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());

      // Reverse list order
      Collections.reverse(searchResults);

      // Render the videos on the index page with passed in List of SearchResults
      return ok(views.html.index.render(searchResults));
    });
  }
}
