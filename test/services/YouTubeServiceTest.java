package services;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.concurrent.CompletionStage;
import com.fasterxml.jackson.databind.JsonNode;
import models.Video;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;


/**
 * Unit tests for the YouTubeService class. Uses Mockito to mock WSClient interactions for testing
 * without calling the actual YouTube API.
 * @author Marjan, Deniz
 */
public class YouTubeServiceTest {

  private YouTubeService youTubeService;
  private WSClient wsClient;
  private WSResponse wsResponse;

  @Before
  public void setUp() {
    wsClient = mock(WSClient.class);
    wsResponse = mock(WSResponse.class);
    youTubeService = mock(YouTubeService.class);
  }

  @Test
  public void testSearchVideos_returnsVideosList() throws Exception {
    // Mock the behavior of WSClient to return a dummy JSON response

    // TODO Write the test for the searchVideos method, this block is just a dummy way of testing whether mockito is working
    when(youTubeService.searchVideos(anyString())).thenReturn(mock(CompletionStage.class));// You'd further mock the response here
    CompletionStage<List<Video>> result = youTubeService.searchVideos("test query");
    assertNotNull(result);



    // old code

//    when(wsClient.url(anyString())).thenReturn(mock(CompletionStage.class));
//    when(wsResponse.asJson())
//        .thenReturn(mock(JsonNode.class)); // You'd further mock the response here
//
//    // Call the searchVideos method
//    CompletionStage<List<Video>> result = youTubeService.searchVideos("test query");
//
//    // Assert the results are as expected (in this case, mock results)
//    assertNotNull(result);
  }
}
