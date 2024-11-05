package controllers;

import scala.concurrent.ExecutionContext;
import services.YouTubeService;

public class YouTubeControllerBuilder {
  private YouTubeService youTubeService;
  private ExecutionContext ignoredEc;

  public YouTubeControllerBuilder setYouTubeService(YouTubeService youTubeService) {
    this.youTubeService = youTubeService;
    return this;
  }

  public YouTubeControllerBuilder setIgnoredEc(ExecutionContext ignoredEc) {
    this.ignoredEc = ignoredEc;
    return this;
  }

  public YouTubeController createYouTubeController() {
    return new YouTubeController(youTubeService, ignoredEc);
  }
}
