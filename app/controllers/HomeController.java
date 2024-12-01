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
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.stream.Materializer;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.YouTubeService;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

/**
 * This controller contains an action to handle HTTP requests to the application's home page. It
 * obtains queries from the user, retrieves, processes, and renders video results to the page.
 *
 * @author Deniz Dinchdonmez, Aynaz, Jessica Chen, Aidassj
 */
public class HomeController extends Controller {

    private final ActorSystem actorSystem;
    private final ActorRef tagsActor;
    private final Materializer materializer;
    private final YouTubeService youTubeService;
    private final ActorRef supervisorActor;

    private final LinkedHashMap<String, List<Video>> multipleQueryResult;
    private static HashMap<String, LinkedHashMap<String, List<Video>>> multipleQueryResults = new HashMap<>();
    private final WSClient wsClient;

    @Inject
    public HomeController(
            ActorSystem actorSystem,
            Materializer materializer,
            WSClient wsClient,
            YouTubeService youTubeService,
            LinkedHashMap<String, List<Video>> multipleQueryResult) {

        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.wsClient = wsClient;
        this.youTubeService = Objects.requireNonNull(youTubeService, "YouTubeService cannot be null");
        this.multipleQueryResult = Objects.requireNonNull(multipleQueryResult, "Query result map cannot be null");
        String uniqueActorName = "supervisorActor-" + UUID.randomUUID().toString();
        this.supervisorActor = actorSystem.actorOf(SupervisorActor.props(null, wsClient), uniqueActorName);
        this.tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService), "tagsActor");
    }

    public HomeController(
            ActorSystem actorSystem,
            Materializer materializer,
            WSClient wsClient,
            YouTubeService youTubeService,
            LinkedHashMap<String, List<Video>> multipleQueryResult,
            HashMap<String, LinkedHashMap<String, List<Video>>> sessionQueryMap) {
        this.actorSystem = Objects.requireNonNull(actorSystem, "ActorSystem cannot be null");
        this.materializer = materializer;
        this.wsClient = wsClient;
        this.youTubeService = Objects.requireNonNull(youTubeService, "YouTubeService cannot be null");
        this.multipleQueryResult = Objects.requireNonNull(multipleQueryResult, "Query result map cannot be null");
        this.multipleQueryResults = sessionQueryMap != null ? sessionQueryMap : new HashMap<>();
        this.tagsActor = actorSystem.actorOf(TagsActor.props(youTubeService), "tagsActor");
        String uniqueActorName = "supervisorActor-" + UUID.randomUUID().toString();
        this.supervisorActor = this.actorSystem.actorOf(SupervisorActor.props(null, wsClient), uniqueActorName);
    }

    public Result index() {
        return ok("Welcome to TubeLytics");
    }

    public CompletionStage<Result> getVideosByTag(String tag) {
        // مستقیماً از CompletionStage استفاده کنید
        CompletionStage<Object> completionStage = Patterns.ask(tagsActor, new Messages.FetchTagsMessage(tag), Duration.ofSeconds(5));

        return completionStage.thenApply(response -> {
            if (response instanceof Messages.TagsResultsMessage) {
                List<Video> videos = ((Messages.TagsResultsMessage) response).getVideos();
                return ok(Json.toJson(videos));
            } else {
                return internalServerError("Unexpected response from TagsActor");
            }
        });
    }


    /**
     * Start of WebSocket connection, which will create a supervisor actor who is in charge of
     * looking after all children actors.
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

    /**
     * Given a query, fetch videos from the YouTube API and render the page.
     *
     * @return a CompletionStage with rendered results or an error message
     */
    public CompletionStage<Result> index(String query, Http.Request request) {
        String sessionId = Optional.ofNullable(request)
                .flatMap(req -> req.cookie("sessionId").map(Http.Cookie::value))
                .orElse(UUID.randomUUID().toString());

        multipleQueryResults.computeIfAbsent(sessionId, k -> new LinkedHashMap<>());
        LinkedHashMap<String, List<Video>> multipleQueryResultFromHashMap = multipleQueryResults.get(sessionId);

        return CompletableFuture.supplyAsync(() -> {
            if (!multipleQueryResultFromHashMap.containsKey(query)) {
                if (multipleQueryResultFromHashMap.size() == 10) {
                    String eldestKey = multipleQueryResultFromHashMap.keySet().iterator().next();
                    multipleQueryResultFromHashMap.remove(eldestKey);
                }
                multipleQueryResultFromHashMap.put(query, new ArrayList<>());
            }
            return query;
        }).thenCompose(queryToFetch -> {
            if (queryToFetch == null || queryToFetch.isEmpty()) {
                return CompletableFuture.completedFuture(List.of());
            }
            return youTubeService.searchVideos(queryToFetch, 10);
        }).thenApply(newVideos -> {
            if (query != null && !query.isEmpty()) {
                List<Video> existingVideos = multipleQueryResultFromHashMap.getOrDefault(query, new ArrayList<>());
                existingVideos.addAll(newVideos);
                multipleQueryResultFromHashMap.put(query, existingVideos);
            }
            List<SearchResult> searchResults = multipleQueryResultFromHashMap.entrySet().stream()
                    .map(entry -> new SearchResult(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            Collections.reverse(searchResults);
            return ok(views.html.reactiveIndex.render(searchResults))
                    .withCookies(Http.Cookie.builder("sessionId", sessionId).build());
        });
    }

    public CompletionStage<Result> wordStats(String query) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Please enter a search term."));
        }

        return youTubeService.searchVideos(query, 50)
                .thenCompose(videos -> {
                    if (videos.isEmpty()) {
                        return CompletableFuture.completedFuture(ok("No videos found for the given query."));
                    }

                    List<String> videoTexts = videos.stream()
                            .map(video -> video.getTitle() + " " + video.getDescription())
                            .collect(Collectors.toList());

                    return Patterns.ask(supervisorActor, new Messages.WordStatsRequest(videoTexts), Duration.ofSeconds(15))
                            .thenApply(response -> {
                                if (response instanceof Messages.WordStatsResponse) {
                                    Messages.WordStatsResponse wordStatsResponse = (Messages.WordStatsResponse) response;

                                    Map<String, Long> wordStatsMap = wordStatsResponse.getWordStats().stream()
                                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                            .collect(Collectors.toMap(
                                                    Map.Entry::getKey,
                                                    Map.Entry::getValue,
                                                    (existing, replacement) -> existing,
                                                    LinkedHashMap::new
                                            ));
                                    return ok(views.html.wordStats.render(wordStatsMap, query));
                                } else {
                                    return internalServerError("Unexpected response from WordStatsActor.");
                                }
                            });
                });
    }

    public CompletionStage<Result> searchByTag(String tag) {
        return youTubeService.searchVideosByTag(tag)
                .thenApply(videos -> {
                    if (videos.isEmpty()) {
                        return notFound("No videos found for tag: " + tag);
                    }
                    return ok(views.html.results.render(videos, "Videos with tag: " + tag));
                });
    }
    public CompletionStage<Result> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return CompletableFuture.completedFuture(badRequest("Please enter a search term."));
        }

        return youTubeService.searchVideos(query, 10)
                .thenApply(videos -> {
                    if (videos.isEmpty()) {
                        return ok("No results found");
                    }
                    return ok(views.html.results.render(videos, query));
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return internalServerError("An error occurred while processing your request.");
                });
    }
    public CompletionStage<Result> getCumulativeWordStats() {
        // ارسال پیام به بازیگر SupervisorActor
        return Patterns.ask(supervisorActor, new Messages.GetCumulativeStats(), Duration.ofSeconds(5))
                .thenApply(response -> {
                    if (response instanceof Messages.WordStatsResponse) {
                        Messages.WordStatsResponse wordStatsResponse = (Messages.WordStatsResponse) response;

                        // مرتب‌سازی نتایج براساس تعداد تکرار کلمات
                        Map<String, Long> sortedWordStats = wordStatsResponse.getWordStats().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (existing, replacement) -> existing,
                                        LinkedHashMap::new
                                ));

                        // بازگرداندن JSON مرتب‌شده
                        return ok(play.libs.Json.toJson(sortedWordStats));
                    } else {
                        // پاسخ غیرمنتظره
                        return internalServerError("Unexpected response from WordStatsActor.");
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return internalServerError("Failed to retrieve cumulative word statistics.");
                });
    }
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
    public CompletionStage<Result> fetchLatestVideos(String channelId) {
        return youTubeService
                .getLast10VideosAsync(channelId)
                .thenApply(videos -> {
                    if (videos.isEmpty()) {
                        return notFound("No videos found for this channel.");
                    }
                    return ok(play.libs.Json.toJson(videos)); // تبدیل لیست ویدیوها به JSON
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError("An error occurred while fetching videos.");
                });
    }
    public CompletionStage<Result> showTags(String videoId) {
        return youTubeService
                .getVideoDetails(videoId)
                .thenApply(video -> {
                    if (video == null) {
                        return notFound("Video not found.");
                    }
                    return ok(views.html.tags.render(video));
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return internalServerError("An error occurred while fetching video tags.");
                });
    }



}
