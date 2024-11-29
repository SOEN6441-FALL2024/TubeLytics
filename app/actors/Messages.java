package actors;

import models.Video;
import java.util.List;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedMap;

/**
 * Class used to better pass responses through actors
 * @author Jessica Chen
 */
public final class Messages {

    /**
     * Class used specifically for passing search results (query, List<Video>) from YouTubeServiceActor to UseActor
     * and UserActor to client
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
            return Objects.equals(searchTerm, that.searchTerm) &&
                    Objects.equals(videos, that.videos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(searchTerm, videos);
        }

        @Override
        public String toString() {
            return "SearchResultsMessage{" +
                    "searchTerm='" + searchTerm + '\'' +
                    ", videos=" + videos +
                    '}';
        }
    }


    /**
     * Error message class to handle errors during communication between actors.
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
            return "ErrorMessage{" +
                    "message='" + message + '\'' +
                    '}';
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




}
