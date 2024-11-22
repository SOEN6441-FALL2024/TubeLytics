package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import play.libs.Json;

public class DummyActor extends AbstractActor {

    public static Props props(ActorRef out) {
        return Props.create(DummyActor.class, out);
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ActorRef out;

    public DummyActor(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    try {
                        JsonNode json = objectMapper.readTree(message);

                        if (json.has("type") && json.get("type").asText().equals("connect")) {
                            JsonNode response = objectMapper.createObjectNode()
                                    .put("type", "responzzzz is hereeeeee")
                                    .put("content", "I am a dummy Pekko created by DD!");
                            out.tell(response.toString(), self());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to parse incoming message as JSON: " + e.getMessage());
                    }
                })
                .build();
    }
}

