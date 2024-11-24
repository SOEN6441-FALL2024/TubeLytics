package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import org.apache.pekko.testkit.TestProbe;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SupervisorActorTest {
    static ActorSystem system;
    private WSClient mockWsClient;
    private WSRequest mockRequest;
    private WSResponse mockResponse;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        MockitoAnnotations.openMocks(this);

        mockRequest = mock(WSRequest.class);
        mockWsClient = mock(WSClient.class);
        mockResponse = mock(WSResponse.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests if supervisorActor is able to forward query to userActor and the userActor to the youtubeServiceActor
     * @author Jessica Chen
     */
    @Test
    public void testSupervisorActorQueryForward() {
        new TestKit(system) {{
            TestProbe youTubeServiceActorProbe = new TestProbe(system);
            TestProbe userActorProbe = new TestProbe(system);
            when(mockWsClient.url(anyString())).thenReturn(mockRequest);
            when(mockRequest.get()).thenReturn(CompletableFuture.completedFuture(mockResponse));

            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(userActorProbe.ref(), mockWsClient));
            String query = "cats";
            supervisorActor.tell(query, getRef());

            userActorProbe.expectMsg(query);
        }};
    }
    /**
     * Tests whether supervisorActor creates children actors
     * @author Jessica Chen
     */
    @Test
    public void testWebSocketActorEmptyQuery() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            assertNotNull(system.actorSelection(supervisorActor.path().child("userActor")));
            assertNotNull(system.actorSelection(supervisorActor.path().child("youTubeServiceActor")));
        }};
    }

    /**
     * Tests supervisorActor with unhandled messages such as int, because right  now it is only handling String
     * @author Jessica Chen
     */
    @Test
    public void testSupervisorActorUnhandledMessage() {
        new TestKit(system) {{
            TestProbe wsProbe = new TestProbe(system);
            ActorRef supervisorActor = system.actorOf(SupervisorActor.props(wsProbe.ref(), mockWsClient));

            supervisorActor.tell(42, getRef());
            expectNoMessage(Duration.ofSeconds(1));
        }};
    }
}
