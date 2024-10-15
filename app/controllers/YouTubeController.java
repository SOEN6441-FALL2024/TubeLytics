package controllers;

import models.Video;
import services.YouTubeService;
import play.mvc.Controller;
import play.mvc.Result;
import play.libs.concurrent.HttpExecutionContext;
import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.List;

/**
 * Controller to handle YouTube video search requests and display results.
 * This class connects the frontend to the backend service (YouTubeService) and returns search results as HTML.
 * 
 * @author Marjan Khassafi
 */
public class YouTubeController extends Controller {

    private final YouTubeService youTubeService;
    private final HttpExecutionContext ec;

    /**
     * Constructor to initialize the YouTubeController with necessary dependencies.
     *
     * @param youTubeService Service to handle YouTube API interactions.
     * @param ec Execution context for asynchronous operations.
     */
    @Inject
    public YouTubeController(YouTubeService youTubeService, HttpExecutionContext ec) {
        this.youTubeService = youTubeService;
        this.ec = ec;
    }

    /**
     * Searches YouTube videos based on a query and returns the results page.
     *
     * @param query The search query provided by the user.
     * @return A CompletionStage that completes with the rendered results page.
     */
    public CompletionStage<Result> search(String query) {
        return youTubeService.searchVideos(query)
               .thenApplyAsync(videos -> ok(views.html.results.render(videos)), ec.current());
    }
}
