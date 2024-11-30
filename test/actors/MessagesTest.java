package actors;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import models.Video;
import org.junit.Test;

public class MessagesTest {

  @Test
  public void testMessageClass() {
    Messages message = new Messages();
    assertNotNull(message);
  }

  @Test
  public void testSearchResultsMessageEquality() {
    // Prepare test data
    List<Video> videos1 = new ArrayList<>();
    videos1.add(
        new Video(
            "Title1",
            "Description1",
            "ChannelId1",
            "VideoId1",
            "ThumbnailUrl1",
            "ChannelTitle1",
            "2024-11-06T04:41:46Z"));

    List<Video> videos2 = new ArrayList<>();
    videos2.add(
        new Video(
            "Title1",
            "Description1",
            "ChannelId1",
            "VideoId1",
            "ThumbnailUrl1",
            "ChannelTitle1",
            "2024-11-06T04:41:46Z"));

    List<Video> videos3 = new ArrayList<>();
    videos3.add(
        new Video(
            "Title2",
            "Description2",
            "ChannelId2",
            "VideoId2",
            "ThumbnailUrl2",
            "ChannelTitle2",
            "2024-11-06T04:41:46Z"));

    // Create objects to compare
    Messages.SearchResultsMessage msg1 = new Messages.SearchResultsMessage("query1", videos1);
    Messages.SearchResultsMessage msg2 = new Messages.SearchResultsMessage("query1", videos2);
    Messages.SearchResultsMessage msg3 = new Messages.SearchResultsMessage("query2", videos3);
    Messages.SearchResultsMessage msg4 = new Messages.SearchResultsMessage("query1", null);

    // Positive test for equality
    assertTrue("Messages with the same searchTerm and videos should be equal.", msg1.equals(msg2));

    // Negative test for different search terms
    assertFalse("Messages with different searchTerm should not be equal.", msg1.equals(msg3));

    // Negative test for different video lists
    assertFalse("Messages with different video lists should not be equal.", msg1.equals(msg4));

    // Test equality with itself
    assertTrue("Message should be equal to itself.", msg1.equals(msg1));

    // Test equality with null
    assertFalse("Message should not be equal to null.", msg1.equals(null));

    // Test equality with an object of a different type
    assertFalse(
        "Message should not be equal to an object of a different type.",
        msg1.equals("Not a SearchResultsMessage"));
  }

  @Test
  public void testSearchResultMessage() {
    String query = "sample";
    List<Video> videos = new ArrayList<>();
    videos.add(
        new Video(
            "Title",
            "Description",
            "ChannelId",
            "VideoId",
            "ThumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z"));

    Messages.SearchResultsMessage msg = new Messages.SearchResultsMessage(query, videos);

    assertEquals("sample", msg.getSearchTerm());
    assertEquals(videos, msg.getVideos());
    assertNotNull(msg);
  }

  @Test
  public void testNullSearchResultsMessage() {
    Messages.SearchResultsMessage message = new Messages.SearchResultsMessage(null, null);

    assertNull(message.getSearchTerm());
    assertNull(message.getVideos());
  }

  @Test
  public void testEmptySearchResultsMessage() {
    Messages.SearchResultsMessage message =
        new Messages.SearchResultsMessage("dogs", new ArrayList<>());

    assertEquals("dogs", message.getSearchTerm());
    assertTrue("There are no videos in this list.", message.getVideos().isEmpty());
    assertNotNull(message);
  }

  @Test
  public void testEqualityAndHashCode() {
    List<Video> videos1 = new ArrayList<>();
    videos1.add(
        new Video(
            "Title1",
            "Description1",
            "Channel1",
            "VideoId1",
            "ThumbnailUrl1",
            "channelTitle1",
            "2024-11-06T04:41:46Z"));

    List<Video> videos2 = new ArrayList<>();
    videos2.add(
        new Video(
            "Title1",
            "Description1",
            "Channel1",
            "VideoId1",
            "ThumbnailUrl1",
            "channelTitle1",
            "2024-11-06T04:41:46Z"));

    Messages.SearchResultsMessage msg1 = new Messages.SearchResultsMessage("query1", videos1);
    Messages.SearchResultsMessage msg2 = new Messages.SearchResultsMessage("query1", videos2);

    // Test equality and hashCode
    assertEquals(msg1, msg2);
    assertEquals(msg1.hashCode(), msg2.hashCode());

    // Negative test for inequality
    Messages.SearchResultsMessage msg3 = new Messages.SearchResultsMessage("query2", videos1);
    assertNotEquals(msg1, msg3);
  }

  @Test
  public void testToString() {
    List<Video> videos = new ArrayList<>();
    videos.add(
        new Video(
            "Title",
            "Description",
            "Channel",
            "VideoId",
            "ThumbnailUrl",
            "channelTitle",
            "2024-11-06T04:41:46Z"));

    Messages.SearchResultsMessage message = new Messages.SearchResultsMessage("query", videos);

    String expectedString =
        "SearchResultsMessage{searchTerm='query', videos=" + videos.toString() + "}";
    assertEquals(expectedString, message.toString());
  }

  @Test
  public void testLargeSearchResultsMessage() {
    String query = "largeQuery";
    List<Video> largeVideoList = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      largeVideoList.add(
          new Video(
              "Title" + i,
              "Description" + i,
              "ChannelId" + i,
              "VideoId" + i,
              "ThumbnailUrl" + i,
              "channelTitle" + i,
              "2024-11-06T04:41:46Z"));
    }

    Messages.SearchResultsMessage message =
        new Messages.SearchResultsMessage(query, largeVideoList);

    assertEquals(1000, message.getVideos().size());
    assertEquals(query, message.getSearchTerm());
  }

  @Test
  public void testErrorMessageConstructorAndGetter() {
    String errorMessageText = "An error occurred";
    Messages.ErrorMessage errorMessage = new Messages.ErrorMessage(errorMessageText);

    assertNotNull(errorMessage);
    assertEquals(
        "The message should match the provided text.", errorMessageText, errorMessage.getMessage());
  }

  @Test
  public void testErrorMessageEquals() {
    // Create ErrorMessage instances
    Messages.ErrorMessage errorMessage1 = new Messages.ErrorMessage("Error Message");
    Messages.ErrorMessage errorMessage2 = new Messages.ErrorMessage("Error Message");
    Messages.ErrorMessage errorMessage3 = new Messages.ErrorMessage("Different Error Message");

    // Test equality with same message content
    assertTrue(
        "ErrorMessage objects with the same message should be equal.",
        errorMessage1.equals(errorMessage2));

    // Test inequality with different message content
    assertFalse(
        "ErrorMessage objects with different messages should not be equal.",
        errorMessage1.equals(errorMessage3));

    // Test equality with the same instance
    assertTrue("ErrorMessage should be equal to itself.", errorMessage1.equals(errorMessage1));

    // Test inequality with null
    assertFalse("ErrorMessage should not be equal to null.", errorMessage1.equals(null));

    // Test inequality with a different type
    assertFalse(
        "ErrorMessage should not be equal to an object of a different type.",
        errorMessage1.equals("A string"));
  }

  @Test
  public void testErrorMessageToString() {
    String errorMessageText = "An error occurred";
    Messages.ErrorMessage errorMessage = new Messages.ErrorMessage(errorMessageText);

    String expectedString = "ErrorMessage{message='An error occurred'}";
    assertEquals(
        "The string representation should match the expected format.",
        expectedString,
        errorMessage.toString());
  }

  @Test
  public void testErrorMessageEqualityAndHashCode() {
    Messages.ErrorMessage errorMessage1 = new Messages.ErrorMessage("Error A");
    Messages.ErrorMessage errorMessage2 = new Messages.ErrorMessage("Error A");
    Messages.ErrorMessage errorMessage3 = new Messages.ErrorMessage("Error B");

    // Print debug information to verify equality comparison
    System.out.println("errorMessage1: " + errorMessage1);
    System.out.println("errorMessage2: " + errorMessage2);
    System.out.println(
        "errorMessage1.equals(errorMessage2): " + errorMessage1.equals(errorMessage2));
    System.out.println("errorMessage1.hashCode(): " + errorMessage1.hashCode());
    System.out.println("errorMessage2.hashCode(): " + errorMessage2.hashCode());

    // Test equality
    assertEquals(
        "ErrorMessage objects with the same message should be equal.",
        errorMessage1,
        errorMessage2);
    assertNotEquals(
        "ErrorMessage objects with different messages should not be equal.",
        errorMessage1,
        errorMessage3);

    // Test hashCode consistency
    assertEquals(
        "Hash codes should match for equal objects.",
        errorMessage1.hashCode(),
        errorMessage2.hashCode());
    assertNotEquals(
        "Hash codes should differ for unequal objects.",
        errorMessage1.hashCode(),
        errorMessage3.hashCode());
  }
}
