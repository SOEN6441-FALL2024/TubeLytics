package models;

import java.util.List;

public class SearchResult {
    public String query;
    public List<Video> videos;

    public SearchResult(String query, List<Video> videos) {
        this.query = query;
        this.videos = videos;
    }
}
