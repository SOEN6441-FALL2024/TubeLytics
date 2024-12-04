package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TagsActorTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TagsActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testGetVideosByTag() {
        new TestKit(system) {{
            // Create TagsActor
            ActorRef tagsActor = system.actorOf(TagsActor.props());

            // Send GetVideosByTag message
            String testTag = "exampleTag";
            tagsActor.tell(new TagsActor.GetVideosByTag(testTag), getRef());

            // Expect VideosByTagResponse
            TagsActor.VideosByTagResponse response = expectMsgClass(TagsActor.VideosByTagResponse.class);

            // Validate response
            assert response.tag.equals(testTag);
            assert response.videos != null && !response.videos.isEmpty();
        }};
    }
}
