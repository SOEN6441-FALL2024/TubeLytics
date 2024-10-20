package controllers;

import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import play.mvc.Controller;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

/**
 * Controller to handle YouTube video search requests and display results.
 */
public class YouTubeController extends Controller {

  public final YouTubeService youTubeService;
  public final ExecutionContext ec; // تغییر به ExecutionContext

  @Inject
  public YouTubeController(YouTubeService youTubeService, ExecutionContext ec) { // تغییر
    this.youTubeService = youTubeService;
    this.ec = ec;
  }

  public CompletionStage search(String query) {
    return youTubeService
            .searchVideos(query)
            .thenApplyAsync(videos -> ok(views.html.results.render(videos))); // تغییر
  }
}
