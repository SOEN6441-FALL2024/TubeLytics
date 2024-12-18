package controllers;

import actors.Messages;
import actors.SupervisorActor;
import actors.TagsActor;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;
import models.SearchResult;
import models.Video;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.stream.Materializer;
import play.libs.streams.ActorFlow;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.YouTubeService;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import javax.inject.Inject;

import actors.Messages;
import actors.SupervisorActor;
import models.ChannelInfo;
import models.SearchResult;
import models.Video;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.stream.Materializer;

import play.libs.streams.ActorFlow;
import play.libs.ws.WSClient;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.YouTubeService;

import java.util.List;
/**
 * This controller contains an action to handle HTTP requests to the application's home page. It
 * obtains queries from the user, retrieves, processes and renders video results to page
 *
 * @author Deniz Dinchdonmez, Aynaz, Jessica Chen, Aidassj
 */
public class HomeController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final YouTubeService youTubeService;
    private ActorRef supervisorActor;
    //private final ActorRef supervisorActor;

    private final LinkedHashMap<String, List<Video>> multipleQueryResult;
    private static HashMap<String, LinkedHashMap<String, List<Video>>> multipleQueryResults =
            new HashMap<>();
    private final WSClient wsClient;

    @Inject
    public HomeController(
            ActorSystem actorSystem,
            Materializer materializer,
            WSClient wsClient,
            YouTubeService youTubeService,
            LinkedHashMap<String, List<Video>> multipleQueryResult) {
        this.actorSystem = Objects.requireNonNull(actorSystem, "ActorSystem cannot be null");
        this.materializer = Objects.requireNonNull(materializer, "Materializer cannot be null");
        this.wsClient = Objects.requireNonNull(wsClient, "WSClient cannot be null");
        this.youTubeService = Objects.requireNonNull(youTubeService, "YouTubeService cannot be null");
        this.multipleQueryResult =
                Objects.requireNonNull(multipleQueryResult, "Query result map cannot be null");

        // Create a unique name for the supervisor actor instance
        String uniqueActorName = "supervisorActor-" + UUID.randomUUID().toString();
        this.supervisorActor = actorSystem.actorOf(SupervisorActor.props(null, wsClient), uniqueActorName);
    }

    public HomeController(
            ActorSystem actorSystem,
            Materializer materializer,
            WSClient wsClient,
            YouTubeService youTubeService,
            LinkedHashMap<String, List<Video>> multipleQueryResult,
            HashMap<String, LinkedHashMap<String, List<Video>>> sessionQueryMap) {
        this.actorSystem = Objects.requireNonNull(actorSystem, "ActorSystem cannot be null");
        this.materializer = Objects.requireNonNull(materializer, "Materializer cannot be null");
        this.wsClient = Objects.requireNonNull(wsClient, "WSClient cannot be null");
        this.youTubeService = Objects.requireNonNull(youTubeService, "YouTubeService cannot be null");
        this.multipleQueryResult =
                Objects.requireNonNull(multipleQueryResult, "Query result map cannot be null");

        // Initialize session-specific query map or default to a new map
        this.multipleQueryResults = sessionQueryMap != null ? sessionQueryMap : new HashMap<>();

        // Create a unique name for the supervisor actor instance
        String uniqueActorName = "supervisorActor-" + UUID.randomUUID().toString();
        this.supervisorActor = this.actorSystem.actorOf(SupervisorActor.props(null, wsClient), uniqueActorName);
    }
    /**
     * Setter for supervisorActor, used for injecting mock in tests.
     *
     * @param supervisorActor the mock ActorRef for testing purposes
     */
    public void setSupervisorActor(ActorRef supervisorActor) {
        this.supervisorActor = supervisorActor;
    }


    /**
     * Start of webSocket connection, which will create a supervisor actor who is in charge of looking
     * after all children actors.
     *
     * @author Jessica Chen
     */
    public WebSocket ws() {
        return WebSocket.Text.accept(
                request -> {
                    return ActorFlow.actorRef(
                            out -> SupervisorActor.props(out, wsClient), actorSystem, materializer);
                });
    }

    public CompletionStage<Result> index(String query) {
        return index(query, null);
    }

    /**
     * Given a query a list of videos are fetched from the youtubeAPI, processed and rendered
     *
     * @return completion stage result of the rendering of given query/queries
     * @author Jessica Chen, Aynaz Javanivayeghan, Deniz Dinchdonmez
     */
    public CompletionStage<Result> index(String query, Http.Request request) {
        // Retrieve session ID from cookies or generate a new one
        String sessionId;

        if (Optional.ofNullable(request).isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        } else {
            sessionId =
                    request.cookie("sessionId").map(Http.Cookie::value).orElse(UUID.randomUUID().toString());
        }

        // Ensure a session-specific map exists
        multipleQueryResults.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());

        // Get the session-specific query results map
        LinkedHashMap<String, List<Video>> multipleQueryResultFromHashMap =
                multipleQueryResults.get(sessionId);

        return CompletableFuture.supplyAsync(
                        () -> {
                            // Handle new or existing queries
                            if (!multipleQueryResultFromHashMap.containsKey(query)) {
                                // If the size exceeds the limit, remove the oldest entry
                                if (multipleQueryResultFromHashMap.size() == 10) {
                                    String eldestKey = multipleQueryResultFromHashMap.keySet().iterator().next();
                                    multipleQueryResultFromHashMap.remove(eldestKey);
                                }
                                // Add the query with an empty list
                                multipleQueryResultFromHashMap.put(query, new ArrayList<>());
                            }
                            return query;
                        })
                .thenCompose(
                        queryToFetch -> {
                            // Handle null or empty queries
                            if (queryToFetch == null || queryToFetch.isEmpty()) {
                                return CompletableFuture.completedFuture(List.of());
                            }
                            // Fetch videos from the YouTube service
                            return youTubeService.searchVideos(queryToFetch, 10);
                        })
                .thenApply(
                        newVideos -> {
                            if (query != null && !query.isEmpty()) {
                                // Retrieve existing videos or initialize a new list
                                List<Video> existingVideos =
                                        multipleQueryResultFromHashMap.getOrDefault(query, new ArrayList<>());

                                // Append new videos to the existing list
                                existingVideos.addAll(newVideos);

                                // Update the query results map
                                multipleQueryResultFromHashMap.put(query, existingVideos);
                            }

                            // Combine results from all queries in the order they were added
                            List<SearchResult> searchResults =
                                    multipleQueryResultFromHashMap.entrySet().stream()
                                            .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                                            .collect(Collectors.toList());

                            // Reverse the order to show the most recent searches at the top
                            Collections.reverse(searchResults);

                            // Render the page with the combined results and set session ID in cookies
                            return ok(views.html.reactiveIndex.render(searchResults))
                                    .withCookies(Http.Cookie.builder("sessionId", sessionId).build());
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

        System.out.println("HomeController: Received query for word stats: " + query);

        // Fetch the latest 50 videos for the query
        return youTubeService.searchVideos(query, 50)
                .thenCompose(videos -> {
                    if (videos.isEmpty()) {
                        System.out.println("HomeController: No videos found for query: " + query);
                        return CompletableFuture.completedFuture(ok("No videos found for the given query."));
                    }

                    // Combine titles and descriptions for processing
                    List<String> videoTexts = videos.stream()
                            .map(video -> video.getTitle() + " " + video.getDescription())
                            .collect(Collectors.toList());

                    System.out.println("HomeController: Processing " + videoTexts.size() + " video texts for word stats.");

                    // Send the request to SupervisorActor for processing
                    return Patterns.ask(supervisorActor, new Messages.WordStatsRequest(videoTexts), Duration.ofSeconds(15))
                            .thenApply(response -> {
                                if (response instanceof Messages.WordStatsResponse) {
                                    Messages.WordStatsResponse wordStatsResponse = (Messages.WordStatsResponse) response;

                                    // Convert to a sorted map by frequency
                                    Map<String, Long> wordStatsMap = wordStatsResponse.getWordStats().stream()
                                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                            .collect(Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    Map.Entry::getValue,
                                                    (existing, replacement) -> existing,
                                                    LinkedHashMap::new
                                            ));

                                    System.out.println("HomeController: Received " + wordStatsMap.size() + " word stats.");
                                    return ok(views.html.wordStats.render(wordStatsMap, query));
                                } else {
                                    System.err.println("HomeController: Unexpected response type from SupervisorActor.");
                                    return internalServerError("Unexpected response from WordStatsActor.");
                                }
                            })
                            .exceptionally(e -> {
                                System.err.println("HomeController: Error occurred while processing word stats: " + e.getMessage());
                                e.printStackTrace();
                                return internalServerError("An error occurred while processing word statistics.");
                            });
                });
    }
    /**
     * Fetch cumulative word stats from WordStatsActor and return them as JSON.
     * @return JSON response with cumulative word stats.
     */
    public CompletionStage<Result> getCumulativeWordStats() {
        return Patterns.ask(supervisorActor, new Messages.GetCumulativeStats(), Duration.ofSeconds(5))
                .thenApply(response -> {
                    if (response instanceof Messages.WordStatsResponse) {
                        Messages.WordStatsResponse wordStatsResponse = (Messages.WordStatsResponse) response;

                        // Use a LinkedHashMap to maintain sorted order in JSON
                        Map<String, Long> sortedWordStats = wordStatsResponse.getWordStats().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Sort by frequency descending
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (existing, replacement) -> existing,
                                        LinkedHashMap::new // Use LinkedHashMap to preserve order
                                ));

                        return ok(play.libs.Json.toJson(sortedWordStats));
                    } else {
                        System.err.println("Unexpected response from WordStatsActor.");
                        return internalServerError("Unexpected response from WordStatsActor.");
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return internalServerError("Failed to retrieve cumulative word statistics.");
                });
    }

    /**
     * Displays the channel profile with all available profile information and the last 10 videos of
     * the channel.
     * @param channelId the ID of the YouTube channel
     * @return a CompletionStage of Result rendering the channel profile page
     * @author Aidassj
     */
    public CompletionStage<Result> channelProfile(String channelId) {
        return youTubeService
                .getChannelInfoAsync(channelId)
                .thenCombine(
                        youTubeService.getLast10VideosAsync(channelId),
                        (channelInfo, videos) -> {
                            if (channelInfo == null) {
                                return internalServerError("An error occurred while fetching channel data.");
                            }
                            return ok(views.html.channelProfile.render(channelInfo, videos));
                        })
                .exceptionally(
                        ex -> {
                            System.err.println("Error fetching channel profile data: " + ex.getMessage());
                            return internalServerError("An error occurred while fetching channel data.");
                        });
    }

    /**
     * API to fetch the latest 10 videos of a channel as JSON.
     * @param channelId the ID of the YouTube channel
     * @return a JSON response containing the list of latest videos
     * @author Aidassj
     */
    public CompletionStage<Result> fetchLatestVideos(String channelId) {
        return youTubeService
                .getLast10VideosAsync(channelId)
                .thenApply(
                        videos -> {
                            if (videos.isEmpty()) {
                                return notFound("No videos found for this channel.");
                            }
                            return ok(play.libs.Json.toJson(videos)); // Convert videos to JSON
                        })
                .exceptionally(
                        ex -> {
                            ex.printStackTrace();
                            return internalServerError("An error occurred while fetching videos.");
                        });
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
    public CompletionStage<Result> searchVideosByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Tag cannot be empty."));
        }

        // Send GetVideosByTag message to SupervisorActor
        return Patterns.ask(supervisorActor, new TagsActor.GetVideosByTag(tag), Duration.ofSeconds(10))
                .thenApply(response -> {
                    if (response instanceof TagsActor.VideosByTagResponse) {
                        TagsActor.VideosByTagResponse tagResponse = (TagsActor.VideosByTagResponse) response;

                        if (tagResponse.videos.isEmpty()) {
                            return notFound("No videos found for the tag: " + tag);
                        }

                        // Convert List<Map<String, String>> to List<Video>
                        List<Video> videoList = tagResponse.videos.stream()
                                .map(videoData -> new Video(
                                        videoData.get("title"),
                                        videoData.get("description"),
                                        videoData.get("channelId"),
                                        videoData.get("videoId"),
                                        videoData.get("thumbnailUrl"),
                                        videoData.get("channelTitle"),
                                        videoData.get("publishedDate")
                                ))
                                .collect(Collectors.toList());

                        // Render the results page with videos
                        return ok(views.html.results.render(videoList, "Videos tagged: " + tag));
                    } else {
                        return internalServerError("Unexpected response from TagsActor.");
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Error occurred while fetching videos by tag: " + e.getMessage());
                    e.printStackTrace();
                    return internalServerError("An error occurred while fetching videos for the tag.");
                });
    }

}
