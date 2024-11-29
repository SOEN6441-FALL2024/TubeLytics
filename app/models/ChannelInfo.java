/**
 * Represents information about a YouTube channel, including its name, description, subscriber
 * count, view count, video count, and ID.
 *
 * <p>This class is immutable and holds data related to a specific channel.
 *
 * @author Aidassj
 */
package models;

public class ChannelInfo {
  private final String name;
  private final String description;
  private final int subscriberCount;
  private final int viewCount;
  private final int videoCount;
  private final String channelId; // Added field for channel ID

  public ChannelInfo(
          String name, String description, int subscriberCount, int viewCount, int videoCount, String channelId) {
    this.name = name;
    this.description = description;
    this.subscriberCount = subscriberCount;
    this.viewCount = viewCount;
    this.videoCount = videoCount;
    this.channelId = channelId; // Initialize the channelId
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getSubscriberCount() {
    return subscriberCount;
  }

  public int getViewCount() {
    return viewCount;
  }

  public int getVideoCount() {
    return videoCount;
  }

  public String getChannelId() { // Getter for channelId
    return channelId;
  }
}
