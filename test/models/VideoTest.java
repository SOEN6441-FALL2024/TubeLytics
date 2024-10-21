package models;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VideoTest {

    @Test
    public void testVideoCreation() {
        Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        assertEquals("Sample Title", video.title());  // بدون get
        assertEquals("Sample Description", video.description());  // بدون get
        assertEquals("channelId123", video.channelId());  // بدون get
        assertEquals("videoId123", video.videoId());  // بدون get
        assertEquals("thumbnailUrl.jpg", video.thumbnailUrl());  // بدون get
    }

    @Test
    public void testVideoEquality() {
        Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        assertEquals(video1, video2);
    }

    @Test
    public void testVideoHashCode() {
        Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        assertNotNull(video.hashCode());
    }
}
