package actors;

import models.Video;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class MessagesTest {

    @Test
    public void testMessageClass() {
        Messages message = new Messages();
        assertNotNull(message);
    }

    @Test
    public void testSearchResultMessage() {
        String query = "sample";
        List<Video> videos = new ArrayList<>();
        videos.add(new Video(
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
        Messages.SearchResultsMessage message = new Messages.SearchResultsMessage("dogs", new ArrayList<>());


        assertEquals("dogs", message.getSearchTerm());
        assertTrue("There are no videos in this list.", message.getVideos().isEmpty());
        assertNotNull(message);
    }
}
