package actors;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

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

    /**
     * Overrides the equals method to compare two SearchResultsMessage objects.
     * Two objects are considered equal if their search terms and video lists are equal.
     * @param o the object to compare with this SearchResultsMessage
     * @return true if the objects are equal, false otherwise
     * @author Aidassj
     */
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      SearchResultsMessage that = (SearchResultsMessage) o;
      return Objects.equals(searchTerm, that.searchTerm) && Objects.equals(videos, that.videos);
    }
    /**
     * Overrides the hashCode method to generate a hash code based on the searchTerm and video list.
     * Ensures that objects considered equal by the equals method produce the same hash code.
     * @return the hash code of this SearchResultsMessage object
     * @author Aidassj
     */

    @Override
    public int hashCode() {
      return Objects.hash(searchTerm, videos);
    }
    /**
     * Overrides the toString method to provide a string representation of the SearchResultsMessage object.
     * The string includes the search term and the list of videos associated with the message.
     * @author Aidassj
     */
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

  /**
   * ErrorMessage class represents an error message for communication between actors.
   * This class is immutable and serializable, providing a structured way to send error messages.
   * It includes methods for retrieving the error message, generating a string representation,
   * and comparing objects for equality.
   * @author Aidassj
   */
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


  /**
   * WordStatsRequest encapsulates a list of video texts for processing word statistics.
   * @author Aynaz Javanivayeghan
   */

  public static class WordStatsRequest implements Serializable {
    private final List<String> videoTexts;

    public WordStatsRequest(List<String> videoTexts) {
      this.videoTexts = videoTexts;
    }

    public List<String> getVideoTexts() {
      return videoTexts;
    }
  }

  /**
   * WordStatsResponse provides the resulting word statistics as a sorted list of entries.
   * @author Aynaz Javanivayeghan
   */

  public static class WordStatsResponse implements Serializable {
    private final List<SortedMap.Entry<String, Long>> wordStats;

    public WordStatsResponse(List<SortedMap.Entry<String, Long>> wordStats) {
      this.wordStats = wordStats;
    }

    public List<SortedMap.Entry<String, Long>> getWordStats() {
      return wordStats;
    }
  }


  /**
   * GetCumulativeStats is a message to request cumulative word statistics from WordStatsActor.
   * This message contains no additional fields and serves as a trigger for fetching stats.
   * @author Aynaz Javanivayeghan
   */

  public static final class GetCumulativeStats implements Serializable {
    private static final long serialVersionUID = 1L;

    // Empty message to request cumulative stats
    public GetCumulativeStats() {}
  }



  public static class SentimentAnalysis {
    private final String sentiment;

    public SentimentAnalysis(String sentiment) {
      this.sentiment = sentiment;
    }

    public String getSentiment() {
      return sentiment;
    }
  }
}
