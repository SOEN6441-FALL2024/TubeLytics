package controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

public class YouTubeControllerBuilderTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testSetYouTubeService() {
    // Mocking YouTubeService
    YouTubeService mockYouTubeService = mock(YouTubeService.class);

    // Creating builder and setting YouTubeService
    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    builder.setYouTubeService(mockYouTubeService);

    // Asserting that the YouTubeService is set correctly
    YouTubeController controller = builder.createYouTubeController();
    assertNotNull(controller);
    assertEquals(mockYouTubeService, controller.youTubeService);
  }

  @Test
  public void testSetIgnoredEc() {
    // Mocking ExecutionContext
    ExecutionContext mockEc = mock(ExecutionContext.class);

    // Creating builder and setting ExecutionContext
    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    builder.setIgnoredEc(mockEc);

    // Asserting that the ExecutionContext is set correctly
    YouTubeController controller = builder.createYouTubeController();
    assertNotNull(controller);
    assertEquals(mockEc, controller.ec);
  }

  @Test
  public void testCreateYouTubeController() {
    // Mocking dependencies
    YouTubeService mockYouTubeService = mock(YouTubeService.class);
    ExecutionContext mockEc = mock(ExecutionContext.class);

    // Creating builder, setting dependencies, and creating the controller
    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    YouTubeController controller =
        builder
            .setYouTubeService(mockYouTubeService)
            .setIgnoredEc(mockEc)
            .createYouTubeController();

    // Asserting the created controller is correct
    assertNotNull(controller);
    assertEquals(mockYouTubeService, controller.youTubeService);
    assertEquals(mockEc, controller.ec);
  }
}
