package actors;

import models.Video;

import java.util.List;

public class Messages {

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
