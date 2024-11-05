package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.Video;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.test.WithApplication;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class YouTubeServiceTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        // Building the application using Guice
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void testVideoFields() {
        Video video = new Video("Title", "Description", "ChannelId", "VideoId", "ThumbnailUrl","channelTitle");

        assertEquals("Title", video.getTitle());
        assertEquals("Description", video.getDescription());
        assertEquals("ChannelId", video.getChannelId());
        assertEquals("VideoId", video.getVideoId());
        assertEquals("ThumbnailUrl", video.getThumbnailUrl());
    }

    @Test
    public void testSearchVideos() throws Exception {
        // Mocking WSClient and WSResponse
        WSClient mockWsClient = mock(WSClient.class);
        WSRequest mockRequest = mock(WSRequest.class);
        WSResponse mockResponse = mock(WSResponse.class);

        // Creating a mock JSON response
        JsonNode mockJson = mock(JsonNode.class);
        when(mockResponse.asJson()).thenReturn(mockJson);
        when(mockJson.get("items")).thenReturn(mock(JsonNode.class));  // Mocking item list

        // Setting up WSClient to return mocked request and response
        when(mockWsClient.url(anyString())).thenReturn(mockRequest);
        when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Injecting mocks into YouTubeService
        YouTubeService youTubeService = new YouTubeService(mockWsClient, mockConfig());

        // Executing the searchVideos method and validating the result
        var result = youTubeService.searchVideos("test query");
        List<Video> videos = result;

        // Verifying that the WSClient was called
        verify(mockWsClient).url(contains("youtube/v3/search"));
    }

    // Mock configuration for testing
    private Config mockConfig() {
        Config mockConfig = mock(Config.class);
        when(mockConfig.getString("youtube.apiKey")).thenReturn("dummy-api-key");
        return mockConfig;
    }
}