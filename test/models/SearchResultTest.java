package models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SearchResultTest {
  private String query;
  private List<Video> testVideos;
  private SearchResult searchResult;

  @Before
  public void setUp() {
    query = "cat";
    testVideos = new ArrayList<>();
    Video video1 = new Video("CatVideoTitle1", "CatVideoDescription1", "CatVideoChannelId1", "CatVideoVideoId1", "CatVideoThumbnailUrl.jpg1", "CatVideoChannelTitle1", "2024-11-06T04:41:46Z", Arrays.asList("tag1", "tag2"));
    Video video2 = new Video("CatVideoTitle2", "CatVideoDescription2", "CatVideoChannelId2", "CatVideoVideoId2", "CatVideoThumbnailUrl.jpg2", "CatVideoChannelTitle2", "2024-11-06T04:41:46Z", Arrays.asList("tag3", "tag4"));
    testVideos.add(video1);
    testVideos.add(video2);
    searchResult = new SearchResult(query, testVideos);
  }

  @Test
  public void testSearchResultConstructor() {
    query = "Panda";
    testVideos = new ArrayList<>();
    Video video1 = new Video("PandaVideoTitle1", "PandaVideoDescription1", "PandaVideoChannelId1", "PandaVideoVideoId1", "PandaVideoThumbnailUrl.jpg1", "PandaVideoChannelTitle1", "2024-11-06T04:41:46Z", Arrays.asList("tag1"));
    Video video2 = new Video("PandaVideoTitle2", "PandaVideoDescription2", "PandaVideoChannelId2", "PandaVideoVideoId2", "PandaVideoThumbnailUrl.jpg2", "PandaVideoChannelTitle2", "2024-11-06T04:41:46Z", Arrays.asList("tag2"));
    testVideos.add(video1);
    testVideos.add(video2);
    SearchResult searchResult1 = new SearchResult(query, testVideos);
    assertNotNull(searchResult1);
    assertEquals("Panda", searchResult1.getQuery());
    assertEquals(testVideos, searchResult1.getVideos());
  }

  @Test
  public void testSearchResultEquality() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1, searchResult2);
    assertTrue("searchResult1 is equal to searchResult2", searchResult1.equals(searchResult2));
    assertTrue(searchResult1.equals(searchResult1));
  }

  @Test
  public void testSearchResultInequality() {
    SearchResult searchResult1 = searchResult;
    String query2 = "dog";
    List<Video> videos2 = new ArrayList<>();
    Video video1 = new Video("DogVideoTitle1", "DogVideoDescription1", "DogVideoChannelId1", "DogVideoVideoId1", "DogVideoThumbnailUrl.jpg1", "DogVideoChannelTitle1", "2024-11-06T04:41:46Z", Arrays.asList("tag5"));
    Video video2 = new Video("DogVideoTitle2", "DogVideoDescription2", "DogVideoChannelId2", "DogVideoVideoId2", "DogVideoThumbnailUrl.jpg2", "DogVideoChannelTitle2", "2024-11-06T04:41:46Z", Arrays.asList("tag6"));
    videos2.add(video1);
    videos2.add(video2);
    SearchResult searchResult2 = new SearchResult(query2, videos2);
    SearchResult searchResult3 = new SearchResult(query, videos2);
    assertNotEquals(searchResult1, searchResult2);
    assertFalse("searchResult1 does not equal searchResult2", searchResult1.equals(searchResult2));
    assertFalse(searchResult1.equals("Dog"));
    assertFalse("searchResult1 does not equal searchResult3", searchResult1.equals(searchResult3));
  }

  @Test
  public void testSearchResultHashCode() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1.hashCode(), searchResult2.hashCode());
  }

  @Test
  public void testSearchResultHashCodeInequality() {
    SearchResult searchResult1 = searchResult;
    String query2 = "dog";
    List<Video> videos2 = new ArrayList<>();
    Video video1 = new Video("DogVideoTitle1", "DogVideoDescription1", "DogVideoChannelId1", "DogVideoVideoId1", "DogVideoThumbnailUrl.jpg1", "DogVideoChannelTitle1", "2024-11-06T04:41:46Z", Arrays.asList("tag5"));
    Video video2 = new Video("DogVideoTitle2", "DogVideoDescription2", "DogVideoChannelId2", "DogVideoVideoId2", "DogVideoThumbnailUrl.jpg2", "DogVideoChannelTitle2", "2024-11-06T04:41:46Z", Arrays.asList("tag6"));
    videos2.add(video1);
    videos2.add(video2);
    SearchResult searchResult2 = new SearchResult(query2, videos2);
    assertNotEquals(searchResult1.hashCode(), searchResult2.hashCode());
  }

  @Test
  public void calculateOverallSentimentTest() {
    List<Video> testVideos = new ArrayList<>();
    for (int i = 0; i < 52; i++) {
      Video video;
      if (i <= 25) {
        video = new Video("Happy Sentiment", "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z", Arrays.asList("happy"));
      } else {
        video = new Video("Sad Sentiment", "Today is a terrible day with awful weather. I am angry and not happy. This is a sad sentence.", "channelId123", "videoId123", "thumbnailUrl.jpg", "channelTitle", "2024-11-06T04:41:46Z", Arrays.asList("sad"));
      }
      testVideos.add(video);
    }
    SearchResult test = new SearchResult("test", testVideos);
    Assert.assertEquals(":-|", test.getOverallSentiment());
  }

  @Test
  public void calculateOverallSentimentTestNull() {
    List<Video> testVideos1 = new ArrayList<>();
    SearchResult test = new SearchResult("test", null);
    Assert.assertEquals("Unavailable", test.getOverallSentiment());
    SearchResult test1 = new SearchResult("test1", testVideos1);
    Assert.assertEquals("Unavailable", test1.getOverallSentiment());
  }

  @Test
  public void testSearchResultEqualsWithItself() {
    assertEquals(searchResult, searchResult);
  }

  @Test
  public void testSearchResultEqualsWithNull() {
    assertNotEquals(searchResult, null);
  }

  @Test
  public void testSearchResultEqualsWithDifferentType() {
    assertNotEquals(searchResult, "a different type");
  }
}
