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
 * @author Jessica Chen, Deniz Dinchdonmez
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

  /**
   * Get the average Flesch-Kincaid Grade Level of the videos in the search result.
   *
   * @return the average Flesch-Kincaid Grade Level
   * @author Deniz Dinchdonmez
   */
  public double getAverageFleschKincaidGradeLevel() {
    return averageFleschKincaidGradeLevel;
  }

  /**
   * Get the average Flesch Reading Ease Score of the videos in the search result.
   *
   * @return the average Flesch Reading Ease Score
   * @author Deniz Dinchdonmez
   */
  public double getAverageFleschReadingEaseScore() {
    return averageFleschReadingEaseScore;
  }

  /**
   * Get the average Flesch-Kincaid Grade Level of the videos in the search result.
   *
   * @param videos the list of videos to calculate the average Flesch-Kincaid Grade Level
   * @return the average Flesch-Kincaid Grade Level
   * @author Deniz Dinchdonmez
   */
  private static double getAverageFleschKincaidGradeLevel(List<Video> videos) {
    if (Optional.ofNullable(videos).isEmpty() || videos.isEmpty()) {
      return 0;
    }
    return videos.stream().mapToDouble(Video::getFleschKincaidGradeLevel).limit(50).average().orElse(0);
  }

  /**
   * Get the average Flesch Reading Ease Score of the videos in the search result.
   *
   * @param videos the list of videos to calculate the average Flesch Reading Ease Score
   * @return the average Flesch Reading Ease Score
   * @author Deniz Dinchdonmez
   */
  private static double getAverageFleschReadingEaseScore(List<Video> videos) {
    if (Optional.ofNullable(videos).isEmpty() || videos.isEmpty()) {
      return 0;
    }
    return videos.stream().mapToDouble(Video::getFleschReadingEaseScore).limit(50).average().orElse(0);
  }
}
