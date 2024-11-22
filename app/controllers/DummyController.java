package controllers;

import actors.DummyActor;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.OverflowStrategy;
import play.api.libs.streams.ActorFlow;
import org.apache.pekko.stream.javadsl.Flow;
import org.apache.pekko.stream.scaladsl.JavaFlowSupport;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;

public class DummyController extends Controller {
  private final ActorSystem actorSystem;
  private final Materializer materializer;

  @Inject
  public DummyController(ActorSystem actorSystem, Materializer materializer) {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
  }

  public WebSocket websocket() {
    return WebSocket.Text.accept(request ->
            Flow.<String>create()
                    .via(ActorFlow.actorRef(
                            out -> DummyActor.props(out),
                            256,
                            OverflowStrategy.dropHead(),
                            actorSystem,
                            materializer
                    ))
    );
  }

  // Render the WebSocket page
  public Result wsDummyPage() {
    return ok(views.html.websocketPage.render("WebSocket Example"));
  }
}
