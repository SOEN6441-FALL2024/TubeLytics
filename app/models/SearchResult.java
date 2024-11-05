package models;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import utils.Helpers;

/**
 * This SearchResult class is created to help us store the information for each search query in
 * order to pass into the scala.html file successfully. Each SearchResult holds a String (query) and
 * a List<Video> (results of query).
 *
 * @author Jessica Chen
 */
public class SearchResult {
  public String query;
  public List<Video> videos;
  private double averageFleschKincaidGradeLevel;
  private double averageFleschReadingEaseScore;

  public SearchResult(String query, List<Video> videos) {
    this.query = query;
    this.videos = videos;
    this.averageFleschKincaidGradeLevel =
        Helpers.formatDouble(getAverageFleschKincaidGradeLevel(videos));
    this.averageFleschReadingEaseScore =
        Helpers.formatDouble(getAverageFleschReadingEaseScore(videos));
  }

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

  public double getAverageFleschKincaidGradeLevel() {
    return averageFleschKincaidGradeLevel;
  }

  public double getAverageFleschReadingEaseScore() {
    return averageFleschReadingEaseScore;
  }

  private static double getAverageFleschKincaidGradeLevel(List<Video> videos) {
    if (Optional.ofNullable(videos).isEmpty() || videos.isEmpty()) {
      return 0;
    }
    return videos.stream().mapToDouble(Video::getFleschKincaidGradeLevel).average().orElse(0);
  }

  private static double getAverageFleschReadingEaseScore(List<Video> videos) {
    if (Optional.ofNullable(videos).isEmpty() || videos.isEmpty()) {
      return 0;
    }
    return videos.stream().mapToDouble(Video::getFleschReadingEaseScore).average().orElse(0);
  }
}
