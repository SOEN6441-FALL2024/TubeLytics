package models;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

public class VideoTest extends WithApplication {

  @Override
  protected Application provideApplication() {
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
            "2024-11-06T04:41:46Z");

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
            "channelTitle",
            "2024-11-06T04:41:46Z");
    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

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
            "2024-11-06T04:41:46Z");
    Video video2 =
        new Video(
            "Different Title",
            "Different Description",
            "channelId456",
            "videoId456",
            "differentThumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

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
            "2024-11-06T04:41:46Z");
    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testSetAndGetTags() {
    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

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
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video2.setTags(Arrays.asList("Tag1", "Tag2"));

    assertEquals(video1, video2);
    assertEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testVideoInequalityWithDifferentTags() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video2.setTags(Arrays.asList("DifferentTag1", "DifferentTag2"));

    assertNotEquals(video1, video2);
    assertNotEquals(video1.hashCode(), video2.hashCode());
  }

  @Test
  public void testVideoEqualsEdgeCases() {
    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video.setTags(Arrays.asList("Tag1", "Tag2"));

    assertNotEquals(video, null);

    assertNotEquals(video, "String Object");

    assertEquals(video, video);

    Video identicalVideo =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    identicalVideo.setTags(Arrays.asList("Tag1", "Tag2"));

    assertEquals(video, identicalVideo);
    assertEquals(video.hashCode(), identicalVideo.hashCode());

    Video differentVideo =
        new Video(
            "Different Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    differentVideo.setTags(Arrays.asList("Tag1", "Tag2"));

    assertNotEquals(video, differentVideo);
  }

  @Test
  public void testGetSubmissionSentiment() {
    // Arrange: Create a video object with a mock sentiment
    String expectedSentiment = "Positive";
    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    // Use reflection to set the private field (if no setter is available)
    try {
      Field field = Video.class.getDeclaredField("submissionSentiment");
      field.setAccessible(true);
      field.set(video, expectedSentiment);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set submissionSentiment field: " + e.getMessage());
    }

    // Act: Retrieve the sentiment using the getter
    String actualSentiment = video.getSubmissionSentiment();

    // Assert: Verify that the retrieved sentiment matches the expected value
    assertEquals(expectedSentiment, actualSentiment);
  }

  @Test
  public void testGetTags() {
    // Case 1: When tags is null
    Video videoWithNullTags =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    videoWithNullTags.setTags(null); // Assuming setTags(null) simulates a null tags value
    List<String> tags = videoWithNullTags.getTags();
    assertNotNull(tags, "Tags should not be null");
    assertTrue(tags.isEmpty(), "Tags should be an empty list when tags is null");

    // Case 2: When tags is an empty list
    Video videoWithEmptyTags =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    videoWithEmptyTags.setTags(Collections.emptyList());
    List<String> emptyTags = videoWithEmptyTags.getTags();
    assertNotNull(emptyTags, "Tags should not be null");
    assertTrue(
        emptyTags.isEmpty(), "Tags should remain empty when explicitly set to an empty list");

    // Case 3: When tags is a non-empty list
    Video videoWithTags =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    List<String> mockTags = Arrays.asList("Tag1", "Tag2", "Tag3");
    videoWithTags.setTags(mockTags);
    List<String> retrievedTags = videoWithTags.getTags();
    assertNotNull(retrievedTags, "Tags should not be null");
    assertEquals(3, retrievedTags.size(), "Tags size should match the set list");
    assertTrue(retrievedTags.contains("Tag1"), "Tags should contain 'Tag1'");
    assertTrue(retrievedTags.contains("Tag2"), "Tags should contain 'Tag2'");
    assertTrue(retrievedTags.contains("Tag3"), "Tags should contain 'Tag3'");
  }

  @Test
  public void testVideoEquals() {
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    // Case 1: Comparing the same object (this == o)
    assertEquals(video1, video1);

    // Case 2: Comparing with null
    assertNotEquals(video1, null);

    // Case 3: Comparing with an object of a different class
    assertNotEquals(video1, "Some String");

    // Case 4: Comparing with another Video object with identical fields
    Video identicalVideo =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    identicalVideo.setTags(Arrays.asList("Tag1", "Tag2"));
    assertEquals(video1, identicalVideo);
    assertEquals(video1.hashCode(), identicalVideo.hashCode());

    // Case 5: Comparing with a Video object with a different title
    Video differentTitleVideo =
        new Video(
            "Different Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    differentTitleVideo.setTags(Arrays.asList("Tag1", "Tag2"));
    assertNotEquals(video1, differentTitleVideo);

    // Case 6: Comparing with a Video object with different tags
    Video differentTagsVideo =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    differentTagsVideo.setTags(Arrays.asList("DifferentTag1", "DifferentTag2"));
    assertNotEquals(video1, differentTagsVideo);

    // Case 7: Comparing with a Video object with a different published date
    Video differentDateVideo =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2025-01-01T00:00:00Z");
    differentDateVideo.setTags(Arrays.asList("Tag1", "Tag2"));
    assertNotEquals(video1, differentDateVideo);
  }

  @Test
  public void testGetTagsCoverage() {
    // Case 1: When tags are null
    Video videoWithoutTags =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    assertNotNull(videoWithoutTags.getTags());
    assertTrue(videoWithoutTags.getTags().isEmpty(), "Expected an empty list when tags are null");

    // Case 2: When tags are an empty list
    videoWithoutTags.setTags(Collections.emptyList());
    assertNotNull(videoWithoutTags.getTags());
    assertTrue(
        videoWithoutTags.getTags().isEmpty(),
        "Expected an empty list when tags are explicitly set to an empty list");

    // Case 3: When tags contain elements
    List<String> mockTags = Arrays.asList("Tag1", "Tag2", "Tag3");
    videoWithoutTags.setTags(mockTags);
    assertEquals(mockTags, videoWithoutTags.getTags());
    assertEquals(3, videoWithoutTags.getTags().size());
    assertTrue(videoWithoutTags.getTags().contains("Tag1"));
    assertTrue(videoWithoutTags.getTags().contains("Tag2"));
    assertTrue(videoWithoutTags.getTags().contains("Tag3"));
  }

  @Test
  public void testEqualsCoverage() {
    // Case 1: Same object (should be equal)
    Video video1 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));
    assertTrue(video1.equals(video1)); // Same object

    // Case 2: Null object (should not be equal)
    assertFalse(video1.equals(null));

    // Case 3: Different class object (should not be equal)
    assertFalse(video1.equals("Some String"));

    // Case 4: Equal objects with same fields
    Video video2 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video2.setTags(Arrays.asList("Tag1", "Tag2"));
    assertTrue(video1.equals(video2));
    assertEquals(video1.hashCode(), video2.hashCode());

    // Case 5: Different title (should not be equal)
    Video video3 =
        new Video(
            "Different Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video3.setTags(Arrays.asList("Tag1", "Tag2"));
    assertFalse(video1.equals(video3));

    // Case 6: Different tags (should not be equal)
    Video video4 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video4.setTags(Arrays.asList("DifferentTag1", "DifferentTag2"));
    assertFalse(video1.equals(video4));

    // Case 7: Null tags in one video (should not be equal)
    Video video5 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video5.setTags(null); // Null tags
    assertFalse(video1.equals(video5));

    // Case 8: Null publishedDate in one video (should not be equal)
    Video video6 =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            null // Null publishedDate
            );
    video6.setTags(Arrays.asList("Tag1", "Tag2"));
    assertFalse(video1.equals(video6));

    // Case 9: Completely different object
    Video video7 =
        new Video(
            "Another Title",
            "Another Description",
            "differentChannelId",
            "differentVideoId",
            "differentThumbnailUrl.jpg",
            "differentChannelTitle",
            "2023-10-15T10:15:30Z");
    video7.setTags(Arrays.asList("AnotherTag1", "AnotherTag2"));
    assertFalse(video1.equals(video7));
  }

  @Test
  public void testGetTagsWithNullTags() {
    // Arrange
    Video video =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    // Act & Assert
    assertNotNull(video.getTags());
    assertTrue(video.getTags().isEmpty(), "Tags should return an empty list when tags is null");
  }

  @Test
  public void testGetTagsWithNonEmptyTags() {
    // Arrange
    Video video =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    List<String> tags = Arrays.asList("Tag1", "Tag2", "Tag3");
    video.setTags(tags);

    // Act
    List<String> retrievedTags = video.getTags();

    // Assert
    assertNotNull(retrievedTags);
    assertEquals(tags.size(), retrievedTags.size());
    assertTrue(retrievedTags.contains("Tag1"));
    assertTrue(retrievedTags.contains("Tag2"));
    assertTrue(retrievedTags.contains("Tag3"));
  }

  @Test
  public void testSetTagsWithNullValue() {
    // Arrange
    Video video =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    // Act
    video.setTags(null);

    // Assert
    assertNotNull(video.getTags());
    assertTrue(video.getTags().isEmpty(), "Tags should be an empty list when set to null");
  }

  @Test
  public void testEqualsMethod() {
    // Arrange
    Video video1 =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    // Case 1: Self-comparison
    assertTrue(video1.equals(video1), "A video should be equal to itself");

    // Case 2: Comparison with null
    assertFalse(video1.equals(null), "A video should not be equal to null");

    // Case 3: Comparison with object of different class
    assertFalse(
        video1.equals("String Object"),
        "A video should not be equal to an object of different class");

    // Case 4: Comparison with identical object
    Video video2 =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video2.setTags(Arrays.asList("Tag1", "Tag2"));
    assertTrue(video1.equals(video2), "Identical videos should be equal");

    // Case 5: Comparison with different object
    Video video3 =
        new Video(
            "Different Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video3.setTags(Arrays.asList("Tag1", "Tag2"));
    assertFalse(video1.equals(video3), "Videos with different titles should not be equal");

    // Case 6: Comparison with different tags
    Video video4 =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video4.setTags(Arrays.asList("DifferentTag1", "DifferentTag2"));
    assertFalse(video1.equals(video4), "Videos with different tags should not be equal");
  }

  @Test
  public void testHashCode() {
    // Arrange
    Video video1 =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video1.setTags(Arrays.asList("Tag1", "Tag2"));

    Video video2 =
        new Video(
            "Title",
            "Description",
            "channelId",
            "videoId",
            "thumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    video2.setTags(Arrays.asList("Tag1", "Tag2"));

    // Act & Assert
    assertEquals(
        video1.hashCode(), video2.hashCode(), "Hash codes of identical videos should be equal");
  }

  @Test
  public void testVideoEqualityWithDifferentAttributes() {
    // Arrange: Create a base video
    Video baseVideo =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    // Case 1: Different description
    Video videoWithDifferentDescription =
        new Video(
            "Sample Title",
            "Different Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    assertNotEquals(baseVideo, videoWithDifferentDescription);

    // Case 2: Different channelId
    Video videoWithDifferentChannelId =
        new Video(
            "Sample Title",
            "Sample Description",
            "differentChannelId",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    assertNotEquals(baseVideo, videoWithDifferentChannelId);

    // Case 3: Different videoId
    Video videoWithDifferentVideoId =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "differentVideoId",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    assertNotEquals(baseVideo, videoWithDifferentVideoId);

    // Case 4: Different thumbnailUrl
    Video videoWithDifferentThumbnailUrl =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "differentThumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");
    assertNotEquals(baseVideo, videoWithDifferentThumbnailUrl);

    // Case 5: Different channelTitle
    Video videoWithDifferentChannelTitle =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "differentChannelTitle",
            "2024-11-06T04:41:46Z");
    assertNotEquals(baseVideo, videoWithDifferentChannelTitle);
  }

  @Test
  public void testGetTagsNull() {
    Video video =
        new Video(
            "Sample Title",
            "Sample Description",
            "channelId123",
            "videoId123",
            "thumbnailUrl.jpg",
            "channelTitle",
            "2024-11-06T04:41:46Z");

    // Case 1: When tags is null
    try {
      Field tagsField = Video.class.getDeclaredField("tags");
      tagsField.setAccessible(true);
      tagsField.set(video, null); // Set the tags field to null
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set tags field to null: " + e.getMessage());
    }
    List<String> tags = video.getTags();
    assertNotNull(tags, "The returned tags list should not be null");
    assertTrue(tags.isEmpty(), "The returned tags list should be empty when tags is null");

    // Case 2: When tags has values
    List<String> mockTags = Arrays.asList("Tag1", "Tag2", "Tag3");
    video.setTags(mockTags); // Use setter to set tags
    tags = video.getTags();
    assertNotNull(tags, "The returned tags list should not be null");
    assertEquals(3, tags.size(), "The size of the returned tags list should match the input");
    assertTrue(tags.contains("Tag1"));
    assertTrue(tags.contains("Tag2"));
    assertTrue(tags.contains("Tag3"));
  }
}
