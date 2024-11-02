package models;

import java.util.Objects;

public class Video {
    private final String title;
    private final String description;
    private final String channelId;
    private final String videoId;
    private final String thumbnailUrl;
    private final String channelTitle;  // New field

    public Video(String title, String description, String channelId, String videoId, String thumbnailUrl, String channelTitle) {
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.videoId = videoId;
        this.thumbnailUrl = thumbnailUrl;
        this.channelTitle = channelTitle;  // Initialize the new field
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

    public String getChannelTitle() {  // Getter for the new field
        return channelTitle;
    }

    // Override equals() and hashCode() to include channelTitle
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(title, video.title) &&
                Objects.equals(description, video.description) &&
                Objects.equals(channelId, video.channelId) &&
                Objects.equals(videoId, video.videoId) &&
                Objects.equals(thumbnailUrl, video.thumbnailUrl) &&
                Objects.equals(channelTitle, video.channelTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, channelId, videoId, thumbnailUrl, channelTitle);
    }
}
