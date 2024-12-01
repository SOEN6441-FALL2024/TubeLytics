package actors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import models.Video;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Helpers;

/**
 * User actor who talks to the client and gets the information needed from others
 *
 * @author Marjan Khassafi, Jessica Chen
 */
public class UserActor extends AbstractActor {
  private static final Logger log = LoggerFactory.getLogger(UserActor.class);
  private final ActorRef ws;
  private final ActorRef youTubeServiceActor;
  private final ActorRef readabilityActor;
  private final Set<String> processedQueries = new HashSet<>();
  private final LinkedList<Video> cumulativeResults =
      new LinkedList<>(); // Stores the latest 10 results

  public static Props props(
      final ActorRef wsOut, final ActorRef youTubeServiceActor, final ActorRef readabilityActor) {
    return Props.create(UserActor.class, wsOut, youTubeServiceActor, readabilityActor);
  }

  public UserActor(
      final ActorRef wsOut, final ActorRef youTubeServiceActor, ActorRef readabilityActor) {
    this.ws = wsOut;
    this.youTubeServiceActor = youTubeServiceActor;
    this.readabilityActor = readabilityActor;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(String.class, this::handleSearchQuery)
        .match(Messages.SearchResultsMessage.class, this::processReceivedResults)
        .match(Messages.ReadabilityResultsMessage.class, this::sendResultsToClient)
        .build();
  }

  private void handleSearchQuery(String query) {
    if (processedQueries.contains(query)) {
      System.out.println("Ignoring repeated query: " + query);
      return;
    }
    System.out.println("Processing new query: " + query);
    processedQueries.add(query);
    youTubeServiceActor.tell(query, getSelf());
  }

  private void processReceivedResults(Messages.SearchResultsMessage response) {
    List<Video> videos = response.getVideos() == null ? new ArrayList<>() : response.getVideos();
    System.out.println(
        "UserActor received results for query: "
            + response.getSearchTerm()
            + ", Number of new videos: "
            + videos.size());

    readabilityActor.tell(new Messages.CalculateReadabilityMessage(videos), getSelf());
  }

  private void sendResultsToClient(Messages.ReadabilityResultsMessage readabilityResults) {
    // Safely handle null videos
    List<Video> videos = Optional.ofNullable(readabilityResults.getVideos()).orElse(Collections.emptyList());
    double averageGradeLevel = readabilityResults.getAverageGradeLevel();
    double averageReadingEase = readabilityResults.getAverageReadingEase();

    log.info("UserActor received readability results. Number of videos: {}", videos.size());

    // Add processed videos to the cumulative list
    videos.forEach(video -> {
      if (!cumulativeResults.contains(video)) {
        cumulativeResults.addFirst(video);
      }
    });

    // Ensure we only keep the latest 10 results
    while (cumulativeResults.size() > 10) {
      cumulativeResults.removeLast();
    }

    // Send results to WebSocket
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode json =
              objectMapper
                      .createObjectNode()
                      .put("searchTerm", Optional.ofNullable(readabilityResults.getSearchTerm()).orElse("Unknown"))
                      .put("averageGradeLevel", Helpers.formatDouble(averageGradeLevel))
                      .put("averageReadingEase", Helpers.formatDouble(averageReadingEase))
                      .set("videos", objectMapper.valueToTree(cumulativeResults));
      ws.tell(objectMapper.writeValueAsString(json), getSelf());
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize videos to JSON", e);
    }
  }


}
