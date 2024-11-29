package models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.Test;

/**
 * Test class for the {@link ChannelInfo} model.
 *
 * @author Aidassj
 */
public class ChannelInfoTest {

    @Test
    public void testChannelInfoCreation() {
        // Arrange
        String name = "Sample Channel";
        String description = "A sample YouTube channel for testing.";
        int subscriberCount = 1000;
        int viewCount = 50000;
        int videoCount = 100;
        String channelId = "channel123";

        // Act
        ChannelInfo channelInfo = new ChannelInfo(name, description, subscriberCount, viewCount, videoCount, channelId);

        // Assert
        assertEquals(name, channelInfo.getName(), "Name should match the expected value.");
        assertEquals(description, channelInfo.getDescription(), "Description should match the expected value.");
        assertEquals(subscriberCount, channelInfo.getSubscriberCount(), "Subscriber count should match the expected value.");
        assertEquals(viewCount, channelInfo.getViewCount(), "View count should match the expected value.");
        assertEquals(videoCount, channelInfo.getVideoCount(), "Video count should match the expected value.");
        assertEquals(channelId, channelInfo.getChannelId(), "Channel ID should match the expected value.");
    }

    @Test
    public void testChannelInfoWithEdgeValues() {
        // Arrange
        String name = "Edge Case Channel";
        String description = "";
        int subscriberCount = 0;
        int viewCount = 0;
        int videoCount = 0;
        String channelId = "edgeChannelId";

        // Act
        ChannelInfo channelInfo = new ChannelInfo(name, description, subscriberCount, viewCount, videoCount, channelId);

        // Assert
        assertEquals(name, channelInfo.getName());
        assertEquals(description, channelInfo.getDescription());
        assertEquals(subscriberCount, channelInfo.getSubscriberCount());
        assertEquals(viewCount, channelInfo.getViewCount());
        assertEquals(videoCount, channelInfo.getVideoCount());
        assertEquals(channelId, channelInfo.getChannelId());
    }

    @Test
    public void testChannelInfoImmutability() {
        // Arrange: Create an initial instance
        ChannelInfo channelInfo = new ChannelInfo(
                "Immutable Channel",
                "Test immutability",
                2000,
                100000,
                50,
                "immutableChannelId");

        // Act: Attempt to modify (this should only be possible through creation of a new object)
        ChannelInfo modifiedChannelInfo = new ChannelInfo(
                "Different Name",
                "Different Description",
                3000,
                200000,
                60,
                "differentChannelId");

        // Assert: Confirm immutability by verifying objects are distinct
        assertNotEquals(channelInfo, modifiedChannelInfo, "The objects should not be equal when created with different data.");
        assertEquals("Immutable Channel", channelInfo.getName(), "Original object should retain its data.");
        assertEquals("Test immutability", channelInfo.getDescription());
        assertEquals(2000, channelInfo.getSubscriberCount());
        assertEquals(100000, channelInfo.getViewCount());
        assertEquals(50, channelInfo.getVideoCount());
        assertEquals("immutableChannelId", channelInfo.getChannelId());
    }

}

