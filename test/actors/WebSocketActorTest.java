package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import static org.mockito.Mockito.*;

public class WebSocketActorTest {
    static ActorSystem system;
    private ActorRef out;
    private ActorRef youTubeServiceActor;
    private TestKit testKit;


    @Before
    public void setUp() {
        system = ActorSystem.create();
        testKit = new TestKit(system);

        youTubeServiceActor = mock(ActorRef.class);
        out = mock(ActorRef.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system, Duration.create(5, "seconds"), true);
        system = null;
    }

    @Test
    public void testWebSocketActorQueryForward() {
        final ActorRef webSocketActor = system.actorOf(WebSocketActor.props("session1", youTubeServiceActor, out));
        webSocketActor.tell("Test", testKit.getRef());
        verify(youTubeServiceActor).tell("Test test test", webSocketActor);
    }

    @Test
    public void testWebSocketActorQueryResponseToSender() {
        final ActorRef webSocketActor = system.actorOf(WebSocketActor.props("session1", youTubeServiceActor, out));
        webSocketActor.tell("response", testKit.getRef());
        testKit.expectNoMessage();
        verify(youTubeServiceActor, never()).tell(any(), any());
    }

    @Test
    public void testWebSocketActorEmptyQuery() {
        final ActorRef webSocketActor = system.actorOf(WebSocketActor.props("session1", youTubeServiceActor, out));
        webSocketActor.tell("", testKit.getRef());
        verify(youTubeServiceActor, never()).tell("", webSocketActor);
    }

    @Test
    public void testWebSocketActorMultipleMessages() {
        final ActorRef webSocketActor = system.actorOf(WebSocketActor.props("session1", youTubeServiceActor, out));
        webSocketActor.tell("Test1", testKit.getRef());
        webSocketActor.tell("Test2", testKit.getRef());
        verify(youTubeServiceActor).tell("Test1", testKit.getRef());
        verify(youTubeServiceActor).tell("Test2", testKit.getRef());
    }
}
