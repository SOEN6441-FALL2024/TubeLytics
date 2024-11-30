package actors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import models.Video;

/**
 * Class used to better pass responses through actors
 *
 * @author Jessica Chen
 */
public final class Messages {

  /**
   * Class used specifically for passing search results (query, List<Video>) from
   * YouTubeServiceActor to UseActor and UserActor to client
   *
   * @author Jessica Chen
   */
  public static final class SearchResultsMessage {
    private String searchTerm;
    private List<Video> videos;

    public SearchResultsMessage(String searchTerm, List<Video> videos) {
      this.searchTerm = searchTerm;
      this.videos = videos;
    }

    public String getSearchTerm() {
      return searchTerm;
    }

    public List<Video> getVideos() {
      return videos;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SearchResultsMessage that = (SearchResultsMessage) o;
      return Objects.equals(searchTerm, that.searchTerm) && Objects.equals(videos, that.videos);
    }

    @Override
    public int hashCode() {
      return Objects.hash(searchTerm, videos);
    }

    @Override
    public String toString() {
      return "SearchResultsMessage{"
          + "searchTerm='"
          + searchTerm
          + '\''
          + ", videos="
          + videos
          + '}';
    }
  }

  /** Error message class to handle errors during communication between actors. */
  public static class ErrorMessage implements Serializable {
    private final String message;

    public ErrorMessage(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return "ErrorMessage{" + "message='" + message + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true; // Check for reference equality
      if (o == null || getClass() != o.getClass()) return false; // Check class type
      ErrorMessage that = (ErrorMessage) o; // Cast and compare field
      return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
      return Objects.hash(message); // Use `message` for hashCode calculation
    }
  }

  public static class CalculateReadabilityMessage {
    private final List<Video> videos;

    public CalculateReadabilityMessage(List<Video> videos) {
      this.videos = videos;
    }

    public List<Video> getVideos() {
      return videos;
    }
  }

  public static class ReadabilityResultsMessage {
    private final List<Video> videos;

    public ReadabilityResultsMessage(List<Video> videos) {
      this.videos = videos;
    }

    public List<Video> getVideos() {
      return videos;
    }
  }
}
