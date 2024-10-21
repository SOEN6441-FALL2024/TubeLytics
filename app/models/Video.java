package models;

import java.util.Objects;

/**
 * Represents a YouTube video with basic information such as title, description, channelId, videoId,
 * and thumbnailUrl. This class is used as a data model to store video information fetched from the
 * YouTube Data API.
 *
 * @author Marjan Khassafi
 */
public class Video {
  private final String title;
  private final String description;
  private final String channelId;
  private final String videoId;
  private final String thumbnailUrl;

  /**
   * Constructor for creating a new Video object.
   *
   * @param title        The title of the video.
   * @param description  The description of the video.
   * @param channelId    The ID of the channel that uploaded the video.
   * @param videoId      The unique ID of the video.
   * @param thumbnailUrl The URL of the video's thumbnail.
   */
  public Video(String title, String description, String channelId, String videoId, String thumbnailUrl) {
    this.title = title;
    this.description = description;
    this.channelId = channelId;
    this.videoId = videoId;
    this.thumbnailUrl = thumbnailUrl;
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

  public String title() {
    return title;
  }

  public String description() {
    return description;
  }

  public String channelId() {
    return channelId;
  }

  public String videoId() {
    return videoId;
  }

  public String thumbnailUrl() {
    return thumbnailUrl;
  }

  // Override equals() to compare Video objects by their fields
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Video video = (Video) o;
    return Objects.equals(title, video.title) &&
            Objects.equals(description, video.description) &&
            Objects.equals(channelId, video.channelId) &&
            Objects.equals(videoId, video.videoId) &&
            Objects.equals(thumbnailUrl, video.thumbnailUrl);
  }

  // Override hashCode() to ensure Video objects with the same fields have the same hash code
  @Override
  public int hashCode() {
    return Objects.hash(title, description, channelId, videoId, thumbnailUrl);
  }
}
