/**
 * Represents information about a YouTube channel, including its name, description,
 * subscriber count, view count, and video count.
 * <p>
 * This class is immutable and holds data related to a specific channel.
 * </p>
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

    public ChannelInfo(String name, String description, int subscriberCount, int viewCount, int videoCount) {
        this.name = name;
        this.description = description;
        this.subscriberCount = subscriberCount;
        this.viewCount = viewCount;
        this.videoCount = videoCount;
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
}