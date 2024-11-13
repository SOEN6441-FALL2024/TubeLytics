package models;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1", "tag2"));

    assertEquals("Sample Title", video.getTitle());
    assertEquals("Sample Description", video.getDescription());
    assertEquals("channelId123", video.getChannelId());
    assertEquals("videoId123", video.getVideoId());
    assertEquals("thumbnailUrl.jpg", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
    assertEquals(List.of("tag1", "tag2"), video.getTags());
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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1", "tag2"));
    Video video2 =
            new Video(
                    "Sample Title",
                    "Sample Description",
                    "channelId123",
                    "videoId123",
                    "thumbnailUrl.jpg",
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1", "tag2"));

    assertEquals(video1, video2);
    assertTrue(video1.equals(video1));
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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    Video video2 =
            new Video(
                    "Different Title",
                    "Different Description",
                    "channelId456",
                    "videoId456",
                    "differentThumbnailUrl.jpg",
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag2"));

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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    Video video2 =
            new Video(
                    "Sample Title",
                    "Sample Description",
                    "channelId123",
                    "videoId123",
                    "thumbnailUrl.jpg",
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));

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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    Video video2 =
            new Video(
                    "Different Title",
                    "Different Description",
                    "channelId456",
                    "videoId456",
                    "differentThumbnailUrl.jpg",
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag2"));

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
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));

    assertNotEquals(video, null);
  }

  @Test
  public void testEquals_DifferentClass() {
    Video video1 =
            new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl", "ChannelTitle","2024-11-06T04:41:46Z", List.of("tag1"));
    String differentClassObject = "Not a Video Object";
    assertNotEquals(
            video1,
            differentClassObject,
            "A video should not be equal to an object of a different class");
  }

  @Test
  public void testEquals_DifferentProperties() {
    Video video1 =
            new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl", "ChannelTitle","2024-11-06T04:41:46Z", List.of("tag1"));
    Video video2 =
            new Video(
                    "Different Title",
                    "Description",
                    "ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video2, "Two videos with different titles should not be equal");

    Video video3 =
            new Video(
                    "Title",
                    "Different Description",
                    "ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video3, "Two videos with different descriptions should not be equal");

    Video video4 =
            new Video(
                    "Title",
                    "Description",
                    "Different ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video4, "Two videos with different channel IDs should not be equal");

    Video video5 =
            new Video(
                    "Title",
                    "Description",
                    "ChannelId",
                    "Different VideoId",
                    "ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video5, "Two videos with different video IDs should not be equal");

    Video video6 =
            new Video(
                    "Title",
                    "Description",
                    "ChannelId",
                    "VideoId",
                    "Different ThumbnailUrl",
                    "ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video6, "Two videos with different thumbnail URLs should not be equal");

    Video video7 =
            new Video(
                    "Title",
                    "Description",
                    "ChannelId",
                    "VideoId",
                    "ThumbnailUrl",
                    "Different ChannelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("tag1"));
    assertNotEquals(video1, video7, "Two videos with different channel titles should not be equal");
  }

  /**
   * Tests sentiment analysis for individual Video.
   * @author Jessica Chen
   */
  @Test
  public void getSubmissionSentimentTest() {
    Video video =
            new Video(
                    "Sample Title",
                    "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.",
                    "channelId123",
                    "videoId123",
                    "thumbnailUrl.jpg",
                    "channelTitle",
                    "2024-11-06T04:41:46Z",
                    List.of("happy", "positive"));

    assertEquals(4, video.getHappyWordCount());
    assertEquals(1, video.getSadWordCount());
    assertEquals(":-)", video.getSubmissionSentiment());
  }
}
