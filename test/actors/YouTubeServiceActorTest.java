package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import services.YouTubeService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class YouTubeServiceActorTest {
    static ActorSystem system;
    private ActorRef out;

    private TestKit testKit;

    @Before
    public void setUp() {
        system = ActorSystem.create();
        testKit = new TestKit(system);

        out = mock(ActorRef.class);
    }

    @After
    public void tearDown() {
        TestKit.shutdownActorSystem(system, Duration.create(5, "seconds"), true);
        system = null;
    }

    //TODO: add tests
}
