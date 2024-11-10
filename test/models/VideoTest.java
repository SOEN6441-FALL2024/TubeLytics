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
    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertEquals("Sample Title", video.getTitle());
    assertEquals("Sample Description", video.getDescription());
    assertEquals("channelId123", video.getChannelId());
    assertEquals("videoId123", video.getVideoId());
    assertEquals("thumbnailUrl.jpg", video.getThumbnailUrl());
    assertEquals("channelTitle",video.getChannelTitle());
    assertEquals("2024-11-06T04:41:46Z", video.getPublishedDate());

  }


  @Test
  public void testVideoEquality() {
    Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");
    Video video2 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertEquals(video1, video2);
  }

  @Test
  public void testVideoInequality() {
    Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");
    Video video2 = new Video("Different Title", "Different Description", "channelId456", "videoId456", "differentThumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertNotEquals(video1, video2);
  }

  @Test
  public void testVideoHashCode() {
    Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");
    Video video2 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testVideoHashCodeInequality() {
    Video video1 = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");
    Video video2 = new Video("Different Title", "Different Description", "channelId456", "videoId456", "differentThumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertNotEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testNullEquality() {

    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg","channelTitle","2024-11-06T04:41:46Z");

    assertNotEquals(video, null);
  }

  @Test
  public void testGetPublishedDate() {
    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z");
    assertEquals("2024-11-06T04:41:46Z", video.getPublishedDate());
  }

  // New test for getUrl method
  @Test
  public void testGetUrl() {
    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z");
    String expectedUrl = "https://www.youtube.com/watch?v=videoId123";
    assertEquals(expectedUrl, video.getUrl());
  }
  @Test
  public void testSelfEquality() {
    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z");
    assertEquals(video, video); // Self equality
  }

  @Test
  public void testDifferentClassEquality() {
    Video video = new Video("Sample Title", "Sample Description", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z");
    String differentObject = "Not a Video";
    assertNotEquals(video, differentObject); // Comparison with an object of different class
  }
}