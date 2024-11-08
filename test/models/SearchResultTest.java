package models;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This test class contains equivalence classes to test the SearchResult class. Please note that our
 * SearchResult object should be created only when there is a valid String and List<Video> This
 * logic has been written in the HomeController class.
 *
 * @author Jessica Chen
 */
public class SearchResultTest {
  private String query;
  private List<Video> testVideos;
  private SearchResult searchResult;

  /**
   * Helps set up mock entries to create search result objects with.
   */
  @Before
  public void setUp() {
    query = "cat";
    testVideos = new ArrayList<>();
    Video video1 =
        new Video(
            "CatVideoTitle1",
            "CatVideoDescription1",
            "CatVideoChannelId1",
            "CatVideoVideoId1",
            "CatVideoThumbnailUrl.jpg1",
            "CatVideoChannelTitle1");
    Video video2 =
        new Video(
            "CatVideoTitle2",
            "CatVideoDescription2",
            "CatVideoChannelId2",
            "CatVideoVideoId2",
            "CatVideoThumbnailUrl.jpg2",
            "CatVideoChannelTitle2");
    testVideos.add(video1);
    testVideos.add(video2);

    searchResult = new SearchResult(query, testVideos);
  }

  /**
   * Asserts search result object with valid parameters is created properly and that it is not null.
   * @author Jessica Chen
   */
  @Test
  public void testSearchResultConstructor() {
    query = "Panda";
    testVideos = new ArrayList<>();
    Video video1 =
        new Video(
            "PandaVideoTitle1",
            "PandaVideoDescription1",
            "PandaVideoChannelId1",
            "PandaVideoVideoId1",
            "PandaVideoThumbnailUrl.jpg1",
            "PandaVideoChannelTitle1");
    Video video2 =
        new Video(
            "PandaVideoTitle2",
            "PandaVideoDescription2",
            "PandaVideoChannelId2",
            "PandaVideoVideoId2",
            "PandaVideoThumbnailUrl.jpg2",
            "PandaVideoChannelTitle2");
    testVideos.add(video1);
    testVideos.add(video2);

    SearchResult searchResult1 = new SearchResult(query, testVideos);

    assertNotNull(searchResult1);
    assertEquals("Panda", searchResult1.getQuery());
    assertEquals(testVideos, searchResult1.getVideos());
  }

  /**
   * Asserts that two search result objects with the same parameters are equal
   * @author Jessica Chen
   */
  @Test
  public void testSearchResultEquality() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1, searchResult2);
    assertTrue("searchResult1 is equal to searchResult2", searchResult1.equals(searchResult2));
    assertTrue(searchResult1.equals(searchResult1));
  }

  /**
   * Asserts that two search result objects with different parameters are not equal. Asserts false that search results with the same query but different list of videos are not equal.
   *
   * @author Jessica Chen
   */
  @Test
  public void testSearchResultInequality() {
    SearchResult searchResult1 = searchResult;
    String query2 = "dog";
    List<Video> videos2 = new ArrayList<>();
    Video video1 =
        new Video(
            "DogVideoTitle1",
            "DogVideoDescription1",
            "DogVideoChannelId1",
            "DogVideoVideoId1",
            "DogVideoThumbnailUrl.jpg1",
            "DogVideoChannelTitle1");
    Video video2 =
        new Video(
            "DogVideoTitle2",
            "DogVideoDescription2",
            "DogVideoChannelId2",
            "DogVideoVideoId2",
            "DogVideoThumbnailUrl.jpg2",
            "DogVideoChannelTitle2");
    videos2.add(video1);
    videos2.add(video2);

    SearchResult searchResult2 = new SearchResult(query2, videos2);
    SearchResult searchResult3 = new SearchResult(query, videos2);
    assertNotEquals(searchResult1, searchResult2);
    assertFalse("searchResult1 does not equal searchResult2", searchResult1.equals(searchResult2));
    assertFalse(searchResult1.equals("Dog"));
    assertFalse("searchResult1 does not equal searchResult3", searchResult1.equals(searchResult3));
  }

  /**
   * Asserts that two search result objects with the same parameters are equal in hashCode
   * @author Jessica Chen
   */
  @Test
  public void testSearchResultHashCode() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1.hashCode(), searchResult2.hashCode());
  }

  /**
   * Asserts that two search result objects with different parameters are not equal in hashCode
   * @author Jessica Chen
   */
  @Test
  // Test equivalence class: inequality in HashCode between two Search Result objects
  public void testSearchResultHashCodeInequality() {
    SearchResult searchResult1 = searchResult;
    String query2 = "dog";
    List<Video> videos2 = new ArrayList<>();
    Video video1 =
        new Video(
            "DogVideoTitle1",
            "DogVideoDescription1",
            "DogVideoChannelId1",
            "DogVideoVideoId1",
            "DogVideoThumbnailUrl.jpg1",
            "DogVideoChannelTitle1");
    Video video2 =
        new Video(
            "DogVideoTitle2",
            "DogVideoDescription2",
            "DogVideoChannelId2",
            "DogVideoVideoId2",
            "DogVideoThumbnailUrl.jpg2",
            "DogVideoChannelTitle2");
    videos2.add(video1);
    videos2.add(video2);

    SearchResult searchResult2 = new SearchResult(query2, videos2);
    assertNotEquals(searchResult1.hashCode(), searchResult2.hashCode());
  }

  /**
   * Tests the sentiment overall for a list of videos (up to 50). Added 25 happy sentiment videos and then 27 sad sentiment. The limit is 50 and so the overall sentiment should be balanced out.
   * @author Jessica Chen
   */
  @Test
  public void calculateOverallSentimentTest() {
    List<Video> testVideos = new ArrayList<>();
    for (int i = 0; i < 52; i++) {
      Video video;
      if (i <= 25) {
        video = new Video(
                "Happy Sentiment",
                "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.",
                "channelId123",
                "videoId123",
                "thumbnailUrl.jpg",
                "channelTitle");
      } else {
        video = new Video(
                "Sad Sentiment",
                "Today is a terrible day with awful weather. I am angry and not happy. This is a sad sentence.",
                "channelId123",
                "videoId123",
                "thumbnailUrl.jpg",
                "channelTitle");
      }
      testVideos.add(video);
    }
    SearchResult test = new SearchResult("test", testVideos);
    Assert.assertEquals(":-|", test.getOverallSentiment());
  }
}
