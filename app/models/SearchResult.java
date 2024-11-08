package models;

import java.util.*;

import utils.Helpers;

/**
 * This SearchResult class is created to help us store the information for each search query in
 * order to pass into the scala.html file successfully. Each SearchResult holds a String (query) and
 * a List<Video> (results of query).
 *
 * @author Jessica Chen, Deniz Dinchdonmez
 */
public class SearchResult {
  private String query;
  private List<Video> videos;
  private double averageFleschKincaidGradeLevel;
  private double averageFleschReadingEaseScore;
  private String overallSentiment;

  public SearchResult(String query, List<Video> videos) {
    this.query = query;
    this.videos = videos;
    this.averageFleschKincaidGradeLevel =
        Helpers.formatDouble(getAverageFleschKincaidGradeLevel(videos));
    this.averageFleschReadingEaseScore =
        Helpers.formatDouble(getAverageFleschReadingEaseScore(videos));
    this.overallSentiment = calculateOverallSentiment(videos);
  }

  /**
   * Get the list of videos in search result object
   *
   * @return the list of videos
   * @author Jessica Chen
   */
  public List<Video> getVideos() {
    return videos;
  }

  /**
   * Get the query in search result object
   *
   * @return query inputted by the users
   * @author Jessica Chen
   */
  public String getQuery() {
    return query;
  }

  /**
   * Compares invoking object with given object to assess if it
   *
   * @return query
   * @author Jessica Chen
   */
  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SearchResult)) return false;
    SearchResult that = (SearchResult) o;

    return Objects.equals(query, that.query) && Objects.equals(videos, that.videos);
  }

  /**
   * Ensures consistency in search result object content using HashCode
   *
   * @return query
   * @author Jessica Chen
   */
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

  /**
   * Get overall sentiment of all video results
   *
   * @return an emoji face that correlates to the overall % of happy vs sad words in each video descriptions
   * @author Jessica Chen
   */
  public String getOverallSentiment() {
    return overallSentiment;
  }

  /**
   * Evaluates the overall sentiment based on sentiments of each video in the list of video results from a query
   * @param videos list of videos from a query entered by the users
   * @return an emoji indicating whether the overall sentiment is happy, sad or neutral
   * @author Jessica Chen
   */
  public static String calculateOverallSentiment(List<Video> videos) {
    double totalHappyWordCount = videos.stream().limit(50).mapToDouble(Video::getHappyWordCount).sum();
    double totalSadWordCount = videos.stream().limit(50).mapToDouble(Video::getSadWordCount).sum();

    return Helpers.calculateSentiment(totalHappyWordCount, totalSadWordCount);
  }
}
