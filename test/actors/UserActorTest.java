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

public class UserActorTest {
    private ActorSystem system;
    private ActorRef youTubeServiceActor;
    private TestKit testKit;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        testKit = new TestKit(system);
        youTubeServiceActor = mock(ActorRef.class);
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
    public void testUserActorQueryForward() {
        final ActorRef userActor = system.actorOf(UserActor.props(youTubeServiceActor));
        userActor.tell("cats", testKit.getRef());

        verify(youTubeServiceActor).tell("cats", userActor);
    }

    /**
     * Tests whether parentActor would forward empty query to next actor
     * @author Jessica Chen
     */
    @Test
    public void testParentActorEmptyQuery() {
        final ActorRef parentActor = system.actorOf(UserActor.props(youTubeServiceActor));
        parentActor.tell("", testKit.getRef());
        verify(youTubeServiceActor, never()).tell("", parentActor);
    }


    /**
     * Tests whether parent actor forwards null queries to next actor
     * @author Jessica Chen
     */
    @Test
    public void testParentActorNullQueryForward() {
        final ActorRef parentActor = system.actorOf(UserActor.props(youTubeServiceActor));
        try{
            parentActor.tell(null, testKit.getRef());
            fail("InvalidMessageException should be thrown.");
        } catch (Exception e) {
            assertTrue(e instanceof InvalidMessageException);
        }
    }
}
