package models;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VideoTest {

    @Test
    public void testVideoCreation() {
        Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        assertEquals("Sample Title", video.title());
        assertEquals("Sample Description", video.description());
        assertEquals("channelId123", video.channelId());
        assertEquals("videoId123", video.videoId());
        assertEquals("thumbnailUrl.jpg", video.thumbnailUrl());
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
