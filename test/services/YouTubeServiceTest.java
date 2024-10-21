package services;

import models.Video;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for YouTubeService.
 */
public class YouTubeServiceTest {

  @Test
  public void testVideoFields() {
    Video video = new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl");

    assertEquals("Title", video.getTitle());
    assertEquals("Description", video.getDescription());
    assertEquals("ChannelId", video.getChannelId());
    assertEquals("VideoId", video.getVideoId());
    assertEquals("ThumbnailUrl", video.getThumbnailUrl());
  }
}
