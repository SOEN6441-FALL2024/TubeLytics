package models;

/**
 * Represents a YouTube video with basic information such as title, description, channelId, videoId,
 * and thumbnailUrl. This class is used as a data model to store video information fetched from the
 * YouTube Data API.
 *
 * @author Marjan Khassafi
 */
public record Video(String title, String description, String channelId, String videoId, String thumbnailUrl) {
  /**
   * Constructor for creating a new Video object.
   *
   * @param title        The title of the video.
   * @param description  The description of the video.
   * @param channelId    The ID of the channel that uploaded the video.
   * @param videoId      The unique ID of the video.
   * @param thumbnailUrl The URL of the video's thumbnail.
   */
  public Video {
  }
}
