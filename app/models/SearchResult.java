package models;

import java.util.List;
import java.util.Objects;

/**
 * This SearchResult class is created to help us store the information for each search query in order to pass
 * into the scala.html file successfully. Each SearchResult holds a String (query) and a List<Video> (results of
 * query).
 *
 * @author Jessica Chen
 */

public class SearchResult {
    public String query;
    public List<Video> videos;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchResult)) return false;
        SearchResult that = (SearchResult) o;

        return Objects.equals(query, that.query) && Objects.equals(videos, that.videos);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(query);
        result = 31 * result + Objects.hashCode(videos);
        return result;
    }

    public SearchResult(String query, List<Video> videos) {
        this.query = query;
        this.videos = videos;
    }
}
