package actors;

import models.Video;
import java.util.List;

/**
 * Class used to better pass responses through actors
 * @author Jessica Chen
 */
public class Messages {
    /**
     * Class used specifically for passing search results (query, List<Video>) from YouTubeServiceActor to UseActor
     * and UserActor to client
     * @author Jessica Chen
     */
    public static class SearchResultsMessage {
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
    }
}
