package controllers;

import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

/** Controller to handle YouTube video search requests and display results. */
public class YouTubeController extends Controller {

  public final YouTubeService youTubeService;
  public final ExecutionContext ec; // تغییر به ExecutionContext

  @Inject
  public YouTubeController(YouTubeService youTubeService, ExecutionContext ec) { // تغییر
    this.youTubeService = youTubeService;
    this.ec = ec;
  }

  public Result search(String query) {
    var videos = youTubeService.searchVideos(query);
    return ok(views.html.results.render(videos)); // تغییر
  }
}
