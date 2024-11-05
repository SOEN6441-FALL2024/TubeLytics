package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import scala.jdk.javaapi.CollectionConverters;
import services.YouTubeService;

import javax.inject.Inject;
import java.util.List;

public class HomeController extends Controller {

  private final YouTubeService youTubeService;

  @Inject
  public HomeController(YouTubeService youTubeService) {
    this.youTubeService = youTubeService;
  }

  public Result index(String query) {
    // Fetch videos or return an empty list if query is null
    List<models.Video> videos = (query == null || query.isEmpty()) ?
            List.of() :
            youTubeService.searchVideos(query);

    // Convert Java List to Scala List

    scala.collection.immutable.List<models.Video> scalaVideos = CollectionConverters.asScala(videos).toList();

    // Render the videos on the index page with the query as the search term
    return ok(views.html.index.render(scalaVideos, query != null ? query : ""));
  }
}
