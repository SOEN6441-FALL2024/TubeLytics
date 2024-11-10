package controllers;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import scala.concurrent.ExecutionContext;
import services.YouTubeService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class YouTubeControllerBuilderTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder().build();
  }

  private ExecutionContext mockEc;

  @BeforeEach
  public void setUp() {
    mockEc = Mockito.spy(ExecutionContext.global());
    doReturn(mockEc).when(mockEc).prepare();
  }

  @Test
  public void testSetYouTubeService() {
    YouTubeService mockYouTubeService = mock(YouTubeService.class);

    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    builder.setYouTubeService(mockYouTubeService);

    YouTubeController controller = builder.createYouTubeController();
    assertNotNull(controller);
    assertEquals(mockYouTubeService, controller.youTubeService);
  }

  @Test
  public void testSetIgnoredEc() {

    ExecutionContext mockEc = Mockito.spy(ExecutionContext.global());
    doReturn(mockEc).when(mockEc).prepare();

    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    builder.setIgnoredEc(mockEc);

    YouTubeController controller = builder.createYouTubeController();
    assertNotNull(controller);
    assertEquals(mockEc, controller.ec);
  }

  @Test
  public void testCreateYouTubeController() {
    YouTubeService mockYouTubeService = mock(YouTubeService.class);
    ExecutionContext mockEc = Mockito.spy(ExecutionContext.global());
    doReturn(mockEc).when(mockEc).prepare();

    YouTubeControllerBuilder builder = new YouTubeControllerBuilder();
    YouTubeController controller = builder.setYouTubeService(mockYouTubeService)
            .setIgnoredEc(mockEc)
            .createYouTubeController();

    assertNotNull(controller);
    assertEquals(mockYouTubeService, controller.youTubeService);
    assertEquals(mockEc, controller.ec);
  }
}

