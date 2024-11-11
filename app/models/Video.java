package models;

import java.util.List;
import java.util.Objects;
import java.util.Collections;
import utils.Helpers;

public class Video {
  private final String title;
  private final String description;
  private final String channelId;
  private final String videoId;
  private final String thumbnailUrl;
  private final String channelTitle;
  private final double fleschKincaidGradeLevel;
  private final double fleschReadingEaseScore;
  private final String submissionSentiment;
  private final double happyWordCount;
  private final double sadWordCount;
  private final List<String> tags;

  // سازنده اصلی که tags را به عنوان ورودی می‌گیرد
  public Video(
          String title,
          String description,
          String channelId,
          String videoId,
          String thumbnailUrl,
          String channelTitle,
          List<String> tags) {
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
    this.tags = tags;
  }

  // سازنده جدید بدون پارامتر tags
  public Video(
          String title,
          String description,
          String channelId,
          String videoId,
          String thumbnailUrl,
          String channelTitle) {
    this(title, description, channelId, videoId, thumbnailUrl, channelTitle, Collections.emptyList());
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

  public double getFleschReadingEaseScore() {
    return fleschReadingEaseScore;
  }

  public double getHappyWordCount() { return happyWordCount; }

  public double getSadWordCount() { return sadWordCount; }

  public String getSubmissionSentiment() { return submissionSentiment; }

  public List<String> getTags() {
    return tags;
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
            && Objects.equals(tags, video.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description, channelId, videoId, thumbnailUrl, channelTitle, tags);
  }
}
