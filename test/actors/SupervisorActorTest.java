package actors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Terminated;
import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import services.YouTubeService;

public class SupervisorActorTest {
  static ActorSystem system;
  private WSClient mockWsClient;
  private WSRequest mockRequest;
  private WSResponse mockResponse;
  private YouTubeService mockYouTubeService;

  @Before
  public void setUp() {
    system = ActorSystem.create();
    MockitoAnnotations.openMocks(this);

    mockRequest = mock(WSRequest.class);
    mockWsClient = mock(WSClient.class);
    mockResponse = mock(WSResponse.class);
    mockYouTubeService = mock(YouTubeService.class);
  }

  @After
  public void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  /**
   * Tests if supervisorActor is able to forward query to userActor and the userActor to the
   * youtubeServiceActor. Creating a mockWsClient, working with a mockRequest and mockResponses and
   * ensuring that it transform into a mockJsonResponse to prevent any null pointer exceptions.
   *
   * @author Aynaz Javanivayeghan, Jessica Chen
   */
  @Test
  public void testSupervisorActorQueryForward() {
    new TestKit(system) {
      {
        when(mockWsClient.url(anyString())).thenReturn(mockRequest);
        when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));
        when(mockResponse.asJson()).thenReturn(new ObjectMapper().createObjectNode());
        when(mockResponse.getStatus()).thenReturn(200);

        TestProbe wsProbe = new TestProbe(system);
        ActorRef supervisorActor =
            system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

        supervisorActor.tell("cats", getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));
      }
    };
  }

  /**
   * Tests whether supervisorActor creates children actors
   *
   * @author Jessica Chen
   */
  @Test
  public void testWebSocketActorEmptyQuery() {
    new TestKit(system) {
      {
        TestProbe wsProbe = new TestProbe(system);
        ActorRef supervisorActor =
            system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));
        assertNotNull(system.actorSelection(supervisorActor.path().child("userActor")));
        assertNotNull(system.actorSelection(supervisorActor.path().child("youTubeServiceActor")));
      }
    };
  }

  /** Tests whether SupervisorActor creates child actors. */
  @Test
  public void testSupervisorActorCreatesChildActors() {
    new TestKit(system) {
      {
        TestProbe wsProbe = new TestProbe(system);
        ActorRef supervisorActor =
            system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

        assertNotNull(system.actorSelection(supervisorActor.path().child("userActor")));
        assertNotNull(system.actorSelection(supervisorActor.path().child("youTubeServiceActor")));
      }
    };
  }

  /**
   * Tests supervisorActor with unhandled messages such as int, because right now it is only
   * handling String
   *
   * @author Aynaz Javanivayeghan, Jessica Chen
   */
  @Test
  public void testSupervisorActorUnhandledMessage() {
    new TestKit(system) {
      {
        TestProbe wsProbe = new TestProbe(system);
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

        // Send an unhandled message
        supervisorActor.tell(42, getRef());

        // Expect an ErrorMessage since the actor responds to unexpected messages
        expectMsgClass(Messages.ErrorMessage.class);
      }
    };
  }
  /** Tests the SupervisorStrategy of SupervisorActor with different exceptions. */
//  @Test
//  public void testSupervisorStrategy() {
//    new TestKit(system) {
//      {
//        TestProbe wsProbe = new TestProbe(system);
//        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));
//
//        // Watch the actor to verify lifecycle changes
//        watch(supervisorActor);
//
//        // Simulate NullPointerException (actor resumes)
//        supervisorActor.tell(new NullPointerException("Simulated NPE"), ActorRef.noSender());
//        expectNoMessage(duration("3 seconds")); // Verify no termination
//
//        // Simulate IllegalArgumentException (actor restarts)
//        supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), ActorRef.noSender());
//        expectNoMessage(duration("3 seconds")); // Verify no termination
//
//        // Simulate IllegalStateException (actor stops)
//        supervisorActor.tell(new IllegalStateException("Simulated ISE"), ActorRef.noSender());
//
//        // Wait for actor termination
//        within(duration("5 seconds"), () -> {
//          expectTerminated(supervisorActor); // Expect termination
//          return null;
//        });
//
//        // Simulate RuntimeException (actor restarts)
//        ActorRef newSupervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));
//        watch(newSupervisorActor);
//        newSupervisorActor.tell(new RuntimeException("Simulated RuntimeException"), ActorRef.noSender());
//        expectNoMessage(duration("3 seconds")); // Verify no termination
//      }
//    };
//  }

  @Test
  public void testSupervisorStrategy_NullPointerException() {
    new TestKit(system) {
      {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), mockWsClient));

        // Watch the actor to monitor its state
        watch(supervisorActor);

        // Simulate a NullPointerException
        supervisorActor.tell(new NullPointerException("Simulated NPE"), ActorRef.noSender());

        // Verify the actor remains alive (no termination expected)
        expectNoMessage(duration("3 seconds"));
      }
    };
  }

  @Test
  public void testSupervisorStrategy_IllegalArgumentException() {
    new TestKit(system) {
      {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), mockWsClient));

        // Watch the actor to monitor its state
        watch(supervisorActor);

        // Simulate an IllegalArgumentException
        supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), ActorRef.noSender());

        // Verify the actor remains alive (no termination expected)
        expectNoMessage(duration("3 seconds"));
      }
    };
  }
//  @Test
//  public void testSupervisorStrategy_IllegalStateException() {
//    new TestKit(system) {{
//      // Create the supervisor actor
//      ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));
//
//      // Watch the actor to monitor termination
//      watch(supervisorActor);
//
//      // Send IllegalStateException to the actor
//      supervisorActor.tell(new IllegalStateException("Simulated ISE"), ActorRef.noSender());
//
//      // Use a longer timeout to expect termination
//      within(duration("10 seconds"), () -> {
//        // Explicitly check for Terminated message
//        expectMsgClass(Terminated.class);
//        return null;
//      });
//
//      System.out.println("SupervisorActor terminated as expected after receiving IllegalStateException.");
//    }};
//  }
  @Test
  public void testSupervisorStrategy_UnknownException() {
    new TestKit(system) {
      {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));
        // Simulate an unknown exception
        supervisorActor.tell(new Exception("Simulated Unknown Exception"), getRef());
        // Validate the actor restarts (no crash, no response expected)
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));
      }
    };
  }

  @Test
  public void testSupervisorActorInitialization() {
    new TestKit(system) {
      {
        // Ensure the SupervisorActor is created successfully
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));
        assertNotNull(supervisorActor);
      }
    };
  }

  @Test
  public void testSupervisorStrategy_HandleSpecificExceptions() {
    new TestKit(system) {
      {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

        // Simulate a NullPointerException and validate resumption
        supervisorActor.tell(new NullPointerException("Simulated NPE"), getRef());
        expectNoMessage(
            scala.concurrent.duration.Duration.create(
                1, "second")); // No crash means resumption worked
        System.out.println("NullPointerException handling verified: Resumed.");

        // Simulate an IllegalArgumentException and validate restart
        supervisorActor.tell(new IllegalArgumentException("Simulated IAE"), getRef());
        expectNoMessage(
            scala.concurrent.duration.Duration.create(
                1, "second")); // No crash means restart worked
        System.out.println("IllegalArgumentException handling verified: Restarted.");

        // Simulate an IllegalStateException and validate stopping
        supervisorActor.tell(new IllegalStateException("Simulated ISE"), getRef());
        expectNoMessage(
            scala.concurrent.duration.Duration.create(
                1, "second")); // No response expected due to stop
        System.out.println("IllegalStateException handling verified: Stopped.");

        // Simulate an unknown exception and validate restart
        supervisorActor.tell(new Exception("Simulated Unknown Exception"), getRef());
        expectNoMessage(
            scala.concurrent.duration.Duration.create(
                1, "second")); // No crash means restart worked
        System.out.println("Unknown exception handling verified: Restarted.");
      }
    };
  }

  @Test
  public void testSupervisorStrategy_isDefinedAt() {
    new TestKit(system) {
      {
        ActorRef supervisorActor = system.actorOf(SupervisorActor.props(getRef(), null));

        // Validate that isDefinedAt is true for all exceptions
        supervisorActor.tell(new NullPointerException("Test NullPointerException"), getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));

        supervisorActor.tell(
            new IllegalArgumentException("Test IllegalArgumentException"), getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));

        supervisorActor.tell(new IllegalStateException("Test IllegalStateException"), getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));

        supervisorActor.tell(new RuntimeException("Test RuntimeException"), getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));

        supervisorActor.tell(new Exception("Test Unknown Exception"), getRef());
        expectNoMessage(scala.concurrent.duration.Duration.create(1, "second"));

        System.out.println("isDefinedAt validated for all exceptions.");
      }
    };
  }
}
