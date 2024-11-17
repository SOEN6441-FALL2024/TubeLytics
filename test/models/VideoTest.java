package models;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class VideoTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testVideoCreation() {
    Video video = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );

    assertEquals("Sample Title", video.getTitle());
    assertEquals("Sample Description", video.getDescription());
    assertEquals("channelId123", video.getChannelId());
    assertEquals("videoId123", video.getVideoId());
    assertEquals("thumbnailUrl.jpg", video.getThumbnailUrl());
    assertEquals("channelTitle", video.getChannelTitle());
  }

  @Test
  public void testVideoEquality() {
    Video video1 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    Video video2 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );

    assertEquals(video1, video2);
    assertTrue(video1.equals(video1));
  }

  @Test
  public void testVideoInequality() {
    Video video1 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    Video video2 = new Video(
            "Different Title",
            "Different Description",
            "channelId456",
            "videoId456",
            "differentThumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );

    assertNotEquals(video1, video2);
  }

  @Test
  public void testVideoHashCode() {
    Video video1 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    Video video2 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );

    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testSetAndGetTags() {
    Video video = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );

    // Case 1: Setting null tags
    video.setTags(null);
    assertNotNull(video.getTags());
    assertTrue(video.getTags().isEmpty(), "Tags should be an empty list when set to null");

    // Case 2: Setting empty tags
    video.setTags(Collections.emptyList());
    assertNotNull(video.getTags());
    assertTrue(video.getTags().isEmpty(), "Tags should remain empty when set to an empty list");

    // Case 3: Setting valid tags
    List<String> tags = Arrays.asList("Tag1", "Tag2", "Tag3");
    video.setTags(tags);
    assertEquals(tags, video.getTags());
  }

  @Test
  public void testVideoEqualityWithTags() {
    Video video1 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    Video video2 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    video2.setTags(Arrays.asList("Tag1", "Tag2"));

    assertEquals(video1, video2);
    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testVideoInequalityWithDifferentTags() {
    Video video1 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    Video video2 = new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z"
    );
    video2.setTags(Arrays.asList("DifferentTag1", "DifferentTag2"));

    assertNotEquals(video1, video2);
    assertNotEquals(video1.hashCode(), video2.hashCode());
  }
}
