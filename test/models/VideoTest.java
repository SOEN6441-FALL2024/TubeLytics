package models;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import static org.junit.jupiter.api.Assertions.*;

public class VideoTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        // Building the application using Guice
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void testVideoCreation() {
        Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        // تست همه متدهای get و متدهای بدون get
        assertEquals("Sample Title", video.getTitle());
        assertEquals("Sample Description", video.getDescription());
        assertEquals("channelId123", video.getChannelId());
        assertEquals("videoId123", video.getVideoId());
        assertEquals("thumbnailUrl.jpg", video.getThumbnailUrl());
    }


    @Test
    public void testVideoEquality() {
        Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        // بررسی برابری دو ویدیو مشابه
        assertEquals(video1, video2);
    }

    @Test
    public void testVideoInequality() {
        Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("Different Title", "Different Description", "channelId456", "videoId456", "differentThumbnailUrl.jpg");

        // بررسی عدم برابری دو ویدیو متفاوت
        assertNotEquals(video1, video2);
    }

    @Test
    public void testVideoHashCode() {
        Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        // بررسی برابر بودن hashCode برای ویدیوهای مشابه
        assertEquals(video1.hashCode(), video2.hashCode());
    }

    @Test
    public void testVideoHashCodeInequality() {
        Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");
        Video video2 = new Video("Different Title", "Different Description", "channelId456", "videoId456", "differentThumbnailUrl.jpg");

        // بررسی تفاوت hashCode برای ویدیوهای متفاوت
        assertNotEquals(video1.hashCode(), video2.hashCode());
    }

    @Test
    public void testNullEquality() {

        Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg");

        // بررسی عدم برابری ویدیو با مقدار null
        assertNotEquals(video, null);
    }
}
