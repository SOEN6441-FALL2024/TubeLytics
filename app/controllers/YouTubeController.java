package controllers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import models.ChannelInfo;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;
import services.YouTubeService;



/**
 * Controller to handle YouTube video search requests and display results.
 */
public class YouTubeController extends Controller {
  /**
   * Controller to handle YouTube video search requests and display results.
   */
  public final YouTubeService youTubeService;

  public final ExecutionContext ec;

  @Inject
  public YouTubeController(YouTubeService youTubeService, ExecutionContext ec) {
    this.youTubeService = youTubeService;
    this.ec = (ec != null) ? ec.prepare() : ExecutionContext.global();
  }


  public Result search(String query) {
    try {
      if (query == null || query.trim().isEmpty()) {
        return badRequest("Please enter a search term.");
      }
      List<Video> videos = youTubeService.searchVideos(query);
      if (videos.isEmpty()) {
        return ok("No results found");
      }
      return ok(views.html.results.render(videos, query));
    } catch (RuntimeException e) {
      return internalServerError("An error occurred while processing your request.");
    }
  }


  /**
   * @author Aidassj
   * Method to display the channel profile with all available profile information and the last 10 videos of the channel.
   */
  public Result channelProfile(String channelId) {
    // Check if channelId is null or empty, and return BadRequest if so
    if (channelId == null || channelId.trim().isEmpty()) {
      return badRequest("Invalid channel ID");
    }

    System.out.println("Channel ID received: " + channelId);

    try {
      // Fetch channel information synchronously
      ChannelInfo channelInfo = youTubeService.getChannelInfo(channelId);

      // Check if channel information is null (indicating an error occurred)
      if (channelInfo == null) {
        return internalServerError("An error occurred while fetching channel data.");
      }

      // Fetch the last 10 videos for the channel synchronously
      List<Video> videos = youTubeService.getLast10Videos(channelId);

      // Return the rendered view with channel info and videos
      return ok(views.html.channelProfile.render(channelInfo, videos));
    } catch (RuntimeException ex) {
      // Log error and return an internal server error response
      System.err.println("Error fetching data: " + ex.getMessage());
      return internalServerError("An error occurred while fetching channel data.");
    }
  }


}