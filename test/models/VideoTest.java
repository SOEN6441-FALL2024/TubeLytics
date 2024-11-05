package models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

public class VideoTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testVideoCreation() {
    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");

    assertEquals("Sample Title", video.getTitle());
    assertEquals("Sample Description", video.getDescription());
    assertEquals("channelId123", video.getChannelId());
    assertEquals("videoId123", video.getVideoId());
    assertEquals("thumbnailUrl.jpg", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
  }

  @Test
  public void testVideoEquality() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");
    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");

    assertEquals(video1, video2);
  }

  @Test
  public void testVideoInequality() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");
    Video video2 =
        new Video(
            "Different Title",
            "Different Description",
            "channelId456",
            "videoId456",
            "differentThumbnailUrl.jpg",
            "channelTitle");

    assertNotEquals(video1, video2);
  }

  @Test
  public void testVideoHashCode() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");
    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");

    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testVideoHashCodeInequality() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");
    Video video2 =
        new Video(
            "Different Title",
            "Different Description",
            "channelId456",
            "videoId456",
            "differentThumbnailUrl.jpg",
            "channelTitle");

    assertNotEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testNullEquality() {

    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle");

    assertNotEquals(video, null);
  }
}
