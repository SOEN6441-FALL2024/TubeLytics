package controllers;

import models.ChannelInfo;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Controller to handle YouTube video search requests and display results. */
public class YouTubeController extends Controller {

  public final YouTubeService youTubeService;
  public final ExecutionContext ec;

  @Inject
  public YouTubeController(YouTubeService youTubeService, ExecutionContext ec) {
    this.youTubeService = youTubeService;
    this.ec = ec;
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
   * Calculates and displays word-level statistics for the latest 50 videos based on a given query.
   *
   * <p>This method uses Java 8 Streams to filter the latest 50 videos, splits the text content
   * into words, counts their occurrences, and sorts the result in descending order of frequency.
   * The statistics are displayed in a view with a table format.</p>
   *
   * @param query the search query used to fetch YouTube videos.
   * @return a Result containing the rendered word statistics page with word frequency data.
   * @author Aynaz Javanivayeghan
   */



  public Result wordStats(String query) {

    if (query == null || query.trim().isEmpty()) {
      return badRequest("Please enter a search term.");
    }

    // Get the search results for the query (limit to the latest 50 videos)
    List<Video> videos = youTubeService.searchVideos(query,50).stream()
            .limit(50)
            .collect(Collectors.toList());

    /// Count word frequencies in titles and descriptions
    Map<String, Long> wordStats = videos.stream()
            .flatMap(video -> Arrays.stream((video.getTitle() + " " + video.getDescription()).split("\\W+")))
            .map(String::toLowerCase)
            .filter(word -> !word.isEmpty())
            .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

    // Check if the video list is empty and return a message if no results are found
    if (videos.isEmpty()) {
      return ok("No words found");
    }

    // Sort by frequency in descending order
    Map<String, Long> sortedWordStats = wordStats.entrySet()
            .stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    java.util.LinkedHashMap::new
            ));

    return ok(views.html.wordStats.render(sortedWordStats, query));
  }

  /**
   * @author Aidassj
   * Method to display the channel profile with all available profile information and the last 10 videos of the channel.
   */
  public Result channelProfile(String channelId) {
    System.out.println("Channel ID received: " + channelId);

    try {
      // Fetch channel information synchronously
      ChannelInfo channelInfo = youTubeService.getChannelInfo(channelId);

      // Check if channel information is null (error occurred)
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


