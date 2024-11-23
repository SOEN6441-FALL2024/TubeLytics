package controllers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.ChannelInfo;
import models.SearchResult;
import models.Video;
import play.mvc.Controller;
import play.mvc.Result;
import services.YouTubeService;

/**
 * This controller contains an action to handle HTTP requests to the application's home page. It
 * obtains queries from the user, retrieves, processes and renders video results to page
 *
 * @author Deniz Dinchdonmez, Aynaz, Jessica Chen
 */
public class HomeController extends Controller {

  private final YouTubeService youTubeService;
  private final LinkedHashMap<String, List<Video>> multipleQueryResult;

  @Inject
  public HomeController(
      YouTubeService youTubeService, LinkedHashMap<String, List<Video>> multipleQueryResult) {
    this.youTubeService = youTubeService;
    this.multipleQueryResult = multipleQueryResult;
  }

  /**
   * Given a query a list of videos are fetched from the youtubeAPI, processed and rendered
   *
   * @return completion stage result of the rendering of given query/queries
   * @author Jessica Chen, Aynaz Javanivayeghan, Deniz Dinchdonmez
   */
  public CompletionStage<Result> index(String query) {
    return CompletableFuture.supplyAsync(
            () -> {
              if (!multipleQueryResult.containsKey(query)) {

                if (multipleQueryResult.size() == 10) {
                  String eldestKey = multipleQueryResult.keySet().iterator().next();
                  multipleQueryResult.remove(eldestKey);
                }

                multipleQueryResult.put(query, List.of());

              } else {
                List<Video> videos = multipleQueryResult.get(query);
                multipleQueryResult.remove(query);
                multipleQueryResult.put(query, videos);
              }
              return query;
            })
        .thenCompose(
            queryToFetch -> {
              if (queryToFetch == null || queryToFetch.isEmpty()) {
                return CompletableFuture.completedFuture(List.of());
              }
              return youTubeService.searchVideos(queryToFetch, 10);
            })
        .thenApply(
            videos -> {
              if (query != null && !query.isEmpty()) {
                multipleQueryResult.put(query, videos);
              }
              return multipleQueryResult;
            })
        .thenApply(
            multipleQueryResult -> {
              List<SearchResult> searchResults =
                  multipleQueryResult.entrySet().stream()
                      .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                      .collect(Collectors.toList());

              Collections.reverse(searchResults);

              // Render the index page
              return ok(views.html.index.render(searchResults));
            });
  }

  /**
   * Given a query, search for videos and render the results on the page.
   *
   * @param query the search query entered by the user
   * @return a Result containing the rendered search results page with video data or an error
   *     message if no results are found or an error occurs. @Author Aynaz Javanivayeghan, Deniz
   *     Dinchdonmez
   */
  public CompletionStage<Result> search(String query) {
    // Validate the query and return a bad request response if invalid
    if (query == null || query.trim().isEmpty()) {
      return CompletableFuture.completedFuture(badRequest("Please enter a search term."));
    }

    // Call the asynchronous searchVideos method
    return youTubeService
        .searchVideos(query, 10)
        .thenApply(
            videos -> {
              // If no videos are found, return a response indicating no results
              if (videos.isEmpty()) {
                return ok("No results found");
              }
              // Render the results page with the videos and query
              return ok(views.html.results.render(videos, query));
            })
        .exceptionally(
            e -> {
              // Handle exceptions and return an internal server error response
              e.printStackTrace();
              return internalServerError("An error occurred while processing your request.");
            });
  }

  /**
   * Calculates and displays word-level statistics for the latest 50 videos based on a given query.
   *
   * <p>This method uses Java 8 Streams to filter the latest 50 videos, splits the text content into
   * words, counts their occurrences, and sorts the result in descending order of frequency. The
   * statistics are displayed in a view with a table format. the search query used to fetch YouTube
   * videos.
   *
   * @return a Result containing the rendered word statistics page with word frequency data.
   * @author Aynaz Javanivayeghan
   */
  public CompletionStage<Result> wordStats(String query) {
    if (query == null || query.trim().isEmpty()) {
      return CompletableFuture.completedFuture(badRequest("Please enter a search term."));
    }

    // Fetch the search results asynchronously
    return youTubeService
        .searchVideos(query, 50)
        .thenApply(
            videos -> {
              // Check if the video list is empty and return a message if no results are found
              if (videos.isEmpty()) {
                return ok("No words found");
              }

              // Count word frequencies in titles and descriptions
              Map<String, Long> wordStats =
                  videos.stream()
                      .flatMap(
                          video ->
                              Arrays.stream(
                                  (video.getTitle() + " " + video.getDescription()).split("\\W+")))
                      .map(String::toLowerCase)
                      .filter(word -> !word.isEmpty())
                      .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

              // Sort by frequency in descending order
              Map<String, Long> sortedWordStats =
                  wordStats.entrySet().stream()
                      .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                      .collect(
                          Collectors.toMap(
                              Map.Entry::getKey,
                              Map.Entry::getValue,
                              (e1, e2) -> e1,
                              java.util.LinkedHashMap::new));

              // Render the results
              return ok(views.html.wordStats.render(sortedWordStats, query));
            })
        .exceptionally(
            e -> {
              // Handle exceptions and return an internal server error response
              e.printStackTrace();
              return internalServerError("An error occurred while processing your request.");
            });
  }

  /**
   * @author Aidassj Method to display the channel profile with all available profile information
   *     and the last 10 videos of the channel.
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

  public CompletionStage<Result> showTags(String videoId) {
    return youTubeService
        .getVideoDetails(videoId)
        .thenApply(
            video -> {
              if (video == null) {
                return notFound("Video not found.");
              }
              return ok(views.html.tags.render(video));
            });
  }

  public CompletionStage<Result> searchByTag(String tag) {
    return youTubeService
        .searchVideosByTag(tag)
        .thenApply(
            videos -> {
              if (videos.isEmpty()) {
                return notFound("No videos found for tag: " + tag);
              }
              return ok(views.html.results.render(videos, "Videos with tag: " + tag));
            });
  }
}
