package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

import org.apache.pekko.actor.InvalidMessageException;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SupervisorActorTest {
    static ActorSystem system;
    private ActorRef out;
    private ActorRef parentActor;
    private TestKit testKit;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        testKit = new TestKit(system);

        parentActor = mock(ActorRef.class);
        out = mock(ActorRef.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests if parentActor is able to forward query to the next actor
     * @author Jessica Chen
     */
    @Test
    public void testWebSocketActorQueryForward() {
        final ActorRef webSocketActor = system.actorOf(SupervisorActor.props("session1", parentActor, out));
        webSocketActor.tell("Test", testKit.getRef());
        verify(parentActor).tell("Test", webSocketActor);
    }
    /**
     * Tests whether webSocketActor would forward empty query to next actor
     * @author Jessica Chen
     */
    @Test
    public void testWebSocketActorEmptyQuery() {
        final ActorRef webSocketActor = system.actorOf(SupervisorActor.props("session1", parentActor, out));
        webSocketActor.tell("", testKit.getRef());
        verify(parentActor, never()).tell("", webSocketActor);
    }
    /**
     * Tests whether webSocketActor would forward null query to next actor
     * @author Jessica Chen
     */
    @Test
    public void testWebSocketActorNullQueryForward() {
        final ActorRef webSocketActor = system.actorOf(SupervisorActor.props("session1", parentActor, out));
        try{
            webSocketActor.tell(null, testKit.getRef());
            fail("InvalidMessageException should be thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidMessageException);
        }
    }
}
