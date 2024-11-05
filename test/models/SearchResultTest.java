package models;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
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

  @Test
  // Test equivalence class: Search Result object with valid String and valid List<Video> and not
  // null
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
    assertEquals("Panda", searchResult1.query);
    assertEquals(testVideos, searchResult1.videos);
  }

  @Test
  // Test equivalence class: Search Result object with null String and null List<Video>
  public void testSearchResultNull() {
    SearchResult searchResult1 = new SearchResult(null, null);

    assertNull("String should be null", searchResult1.query);
    assertNull("List<Video> should be null", searchResult1.videos);
  }

  @Test
  // Test equivalence class: equality between two Search Result objects
  public void testSearchResultEquality() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1, searchResult2);
  }

  @Test
  // Test equivalence class: inequality between two Search Result objects
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
    assertNotEquals(searchResult1, searchResult2);
  }

  @Test
  // Test equivalence class: equality in HashCode between two Search Result objects
  public void testSearchResultHashCode() {
    SearchResult searchResult1 = searchResult;
    SearchResult searchResult2 = new SearchResult(query, testVideos);
    assertEquals(searchResult1.hashCode(), searchResult2.hashCode());
  }

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
}
