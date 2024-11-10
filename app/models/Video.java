package models;

import java.util.Objects;
import utils.Helpers;

public class Video {
  private final String title;
  private final String description;
  private final String channelId;
  private final String videoId;
  private final String thumbnailUrl;
  private final String channelTitle; // New field
  private final double fleschKincaidGradeLevel;
  private final double fleschReadingEaseScore;
  private final String submissionSentiment;
  private final double happyWordCount;
  private final double sadWordCount;

  public Video(
      String title,
      String description,
      String channelId,
      String videoId,
      String thumbnailUrl,
      String channelTitle) {
    this.title = title;
    this.description = description;
    this.channelId = channelId;
    this.videoId = videoId;
    this.thumbnailUrl = thumbnailUrl;
    this.channelTitle = channelTitle; // Initialize the new field
    this.fleschKincaidGradeLevel = Helpers.calculateFleschKincaidGradeLevel(description);
    this.fleschReadingEaseScore = Helpers.calculateFleschReadingEaseScore(description);
    this.happyWordCount = Helpers.calculateHappyWordCount(description);
    this.sadWordCount = Helpers.calculateSadWordCount(description);
    this.submissionSentiment = Helpers.calculateSentiment(happyWordCount, sadWordCount);
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getVideoId() {
    return videoId;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public String getChannelTitle() { // Getter for the new field
    return channelTitle;
  }

  public double getFleschKincaidGradeLevel() {
    return fleschKincaidGradeLevel;
  }

  public double getFleschReadingEaseScore() {
    return fleschReadingEaseScore;
  }

  public double getHappyWordCount() { return happyWordCount; }

  public double getSadWordCount() { return sadWordCount; }

  public String getSubmissionSentiment() { return submissionSentiment; }

  // Override equals() and hashCode() to include channelTitle
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Video video = (Video) o;
    return Objects.equals(title, video.title)
        && Objects.equals(description, video.description)
        && Objects.equals(channelId, video.channelId)
        && Objects.equals(videoId, video.videoId)
        && Objects.equals(thumbnailUrl, video.thumbnailUrl)
        && Objects.equals(channelTitle, video.channelTitle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, channelId, videoId, thumbnailUrl, channelTitle);
  }
}
