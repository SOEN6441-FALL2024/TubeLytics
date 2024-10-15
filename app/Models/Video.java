package models;

/**
 * Represents a YouTube video with basic information such as title, description, channelId, videoId, and thumbnailUrl.
 * This class is used as a data model to store video information fetched from the YouTube Data API.
 * 
 * @author Marjan Khassafi
 */
public class Video {
    private String title;
    private String description;
    private String channelId;
    private String videoId;
    private String thumbnailUrl;

    /**
     * Constructor for creating a new Video object.
     *
     * @param title The title of the video.
     * @param description The description of the video.
     * @param channelId The ID of the channel that uploaded the video.
     * @param videoId The unique ID of the video.
     * @param thumbnailUrl The URL of the video's thumbnail.
     */
    public Video(String title, String description, String channelId, String videoId, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.videoId = videoId;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getters for all the fields
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getChannelId() { return channelId; }
    public String getVideoId() { return videoId; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
