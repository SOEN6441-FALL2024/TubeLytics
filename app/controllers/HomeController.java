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
import play.mvc.Http;
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
  private HashMap<String, LinkedHashMap<String, List<Video>>> queryResults = new HashMap<>();

  @Inject
  public HomeController(
      YouTubeService youTubeService, LinkedHashMap<String, List<Video>> multipleQueryResult) {
    this.youTubeService = youTubeService;
    this.multipleQueryResult = multipleQueryResult;
  }

  public CompletionStage<Result> index(String query){
    return index(query, null);
  }

  /**
   * Given a query a list of videos are fetched from the youtubeAPI, processed and rendered
   *
   * @return completion stage result of the rendering of given query/queries
   * @author Jessica Chen, Aynaz Javanivayeghan
   */
  public CompletionStage<Result> index(String query, Http.Request request) {

    String sessionId = request.session().get("sessionId").orElse(UUID.randomUUID().toString());

    if (!queryResults.containsKey(sessionId)) {
      queryResults.put(sessionId, new LinkedHashMap<>());
    }

    LinkedHashMap<String, List<Video>> multipleQueryResultFromHashMap = queryResults.get(sessionId);


    return CompletableFuture.supplyAsync(
            () -> {
              // If query is new
              if (!multipleQueryResultFromHashMap.containsKey(query)) {
                // If size of map is at 10 already, delete the oldest entry
                if (multipleQueryResultFromHashMap.size() == 10) {
                  String eldestKey = multipleQueryResultFromHashMap.keySet().iterator().next();
                  multipleQueryResultFromHashMap.remove(eldestKey);
                }
                // Fetch videos or return an empty list if query is null
                List<models.Video> videos =
                    (query == null || query.isEmpty())
                        ? List.of()
                        : youTubeService.searchVideos(query);

                // Add query entry to Map
                multipleQueryResultFromHashMap.put(query, videos);
              } else {
                List<Video> videos = multipleQueryResultFromHashMap.get(query);
                multipleQueryResultFromHashMap.remove(query);
                multipleQueryResultFromHashMap.put(query, videos);
              }
              return multipleQueryResultFromHashMap;
            })
        .thenApply(
            multipleQueryResult -> {
              // Create SearchResult objects to hold query and List<Video> based on Map
              List<SearchResult> searchResults =
                  multipleQueryResult.entrySet().stream()
                      .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                      .collect(Collectors.toList());

              // Reverse list order
              Collections.reverse(searchResults);

              // Render the videos on the index page with passed in List of SearchResults
              return ok(views.html.index.render(searchResults)).withCookies(Http.Cookie.builder("sessionId", sessionId).build());
            });
  }

  /**
   * Given a query, search for videos and render the results on the page.
   *
   * @param query the search query entered by the user
   * @return a Result containing the rendered search results page with video data or an error
   *     message if no results are found or an error occurs.
   */
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
   * <p>This method uses Java 8 Streams to filter the latest 50 videos, splits the text content into
   * words, counts their occurrences, and sorts the result in descending order of frequency. The
   * statistics are displayed in a view with a table format. the search query used to fetch YouTube
   * videos.
   *
   * @return a Result containing the rendered word statistics page with word frequency data.
   * @author Aynaz Javanivayeghan
   */
  public Result wordStats(String query) {

    if (query == null || query.trim().isEmpty()) {
      return badRequest("Please enter a search term.");
    }

    // Get the search results for the query (limit to the latest 50 videos)
    List<Video> videos =
        youTubeService.searchVideos(query, 50).stream().limit(50).collect(Collectors.toList());

    /// Count word frequencies in titles and descriptions
    Map<String, Long> wordStats =
        videos.stream()
            .flatMap(
                video ->
                    Arrays.stream((video.getTitle() + " " + video.getDescription()).split("\\W+")))
            .map(String::toLowerCase)
            .filter(word -> !word.isEmpty())
            .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

    // Check if the video list is empty and return a message if no results are found
    if (videos.isEmpty()) {
      return ok("No words found");
    }

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

    return ok(views.html.wordStats.render(sortedWordStats, query));
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
