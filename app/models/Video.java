package models;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import utils.Helpers;

public class Video {
  private final String title;
  private final String description;
  private final String channelId;
  private final String videoId;
  private final String thumbnailUrl;
  private final String channelTitle;
  private double fleschKincaidGradeLevel;
  private double fleschReadingEaseScore;
  private final String submissionSentiment;
  private final double happyWordCount;
  private final double sadWordCount;
  private final String publishedDate;
  private List<String> tags;

  public Video(
          String title,
          String description,
          String channelId,
          String videoId,
          String thumbnailUrl,
          String channelTitle,
          String publishedDate) {
    this.title = title;
    this.description = description;
    this.channelId = channelId;
    this.videoId = videoId;
    this.thumbnailUrl = thumbnailUrl;
    this.channelTitle = channelTitle;
    this.fleschKincaidGradeLevel = Helpers.calculateFleschKincaidGradeLevel(description);
    this.fleschReadingEaseScore = Helpers.calculateFleschReadingEaseScore(description);
    this.happyWordCount = Helpers.calculateHappyWordCount(description);
    this.sadWordCount = Helpers.calculateSadWordCount(description);
    this.submissionSentiment = Helpers.calculateSentiment(happyWordCount, sadWordCount);
    this.publishedDate = publishedDate;
    this.tags = Collections.emptyList();
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

  public String getChannelTitle() {
    return channelTitle;
  }

  public double getFleschKincaidGradeLevel() {
    return fleschKincaidGradeLevel;
  }

  public void setFleschKincaidGradeLevel(double fleschKincaidGradeLevel) {
    this.fleschKincaidGradeLevel = fleschKincaidGradeLevel;
  }

  public double getFleschReadingEaseScore() {
    return fleschReadingEaseScore;
  }

  public void setFleschReadingEaseScore(double fleschReadingEaseScore) {
    this.fleschReadingEaseScore = fleschReadingEaseScore;
  }

  public double getHappyWordCount() {
    return happyWordCount;
  }

  public double getSadWordCount() {
    return sadWordCount;
  }

  public String getSubmissionSentiment() {
    return submissionSentiment;
  }

  public String getPublishedDate() {
    return publishedDate;
  }

  public String getUrl() {
    return "https://www.youtube.com/watch?v=" + videoId;
  }

  public List<String> getTags() {
    return tags == null ? Collections.emptyList() : tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags == null ? Collections.emptyList() : tags;
  }

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
            && Objects.equals(channelTitle, video.channelTitle)
            && Objects.equals(publishedDate, video.publishedDate)
            && Objects.equals(tags, video.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
            title, description, channelId, videoId, thumbnailUrl, channelTitle, publishedDate, tags);
  }

  @Override
  public String toString() {
    return "Video{" +
            "title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", channelId='" + channelId + '\'' +
            ", videoId='" + videoId + '\'' +
            ", thumbnailUrl='" + thumbnailUrl + '\'' +
            ", channelTitle='" + channelTitle + '\'' +
            ", publishedDate='" + publishedDate + '\'' +
            ", tags=" + tags +
            '}';
  }
}
