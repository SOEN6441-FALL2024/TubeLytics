package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WordStatsActor processes and maintains cumulative word statistics from video texts.
 * It filters stopwords, calculates word frequencies, and provides the top 50 words by frequency.
 * @author Aynaz Javanivayeghan
 */
public class WordStatsActor extends AbstractActor {

    // Persistent cumulative word stats
    private final SortedMap<String, Long> cumulativeWordFrequencies = new TreeMap<>();

    public static Props props() {
        return Props.create(WordStatsActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Messages.WordStatsRequest.class, request -> {
                    // Extract video texts
                    List<String> videoTexts = request.getVideoTexts();

                    // Check for null or empty input
                    if (videoTexts == null || videoTexts.isEmpty()) {
                        System.err.println("WordStatsActor: Received empty or null video texts.");
                        getSender().tell(new Messages.WordStatsResponse(Collections.emptyList()), getSelf());
                        return;
                    }

                    // Log input size for debugging
                    System.out.println("WordStatsActor: Processing " + videoTexts.size() + " video texts.");
                    long startTime = System.currentTimeMillis();

                    // Calculate word frequencies for this batch
                    SortedMap<String, Long> newWordFrequencies = videoTexts.stream()
                            .flatMap(text -> Arrays.stream(text.split("\\W+"))) // Split on non-word characters
                            .map(String::toLowerCase) // Convert to lowercase
                            .filter(word -> word.length() > 2)
                            .filter(word -> word.matches("[a-z]+")) // Filter out non-alphabetic words
                            .filter(word -> !isStopWord(word))
                            .collect(Collectors.toMap(
                                    word -> word,
                                    word -> 1L,
                                    Long::sum, // Merge counts by summing them
                                    TreeMap::new)); // Use TreeMap to keep it sorted

                    // Merge new frequencies with cumulative stats
                    newWordFrequencies.forEach((word, count) ->
                            cumulativeWordFrequencies.merge(word, count, Long::sum)
                    );

                    long endTime = System.currentTimeMillis();
                    System.out.println("WordStatsActor: Processing completed in " + (endTime - startTime) + " ms.");
                    System.out.println("WordStatsActor: Updated cumulative stats with " + newWordFrequencies.size() + " new words.");

                    // Sort cumulative stats by frequency and limit to top 50
                    List<Map.Entry<String, Long>> sortedWordStats = cumulativeWordFrequencies.entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed() // Sort by frequency descending
                                    .thenComparing(Map.Entry::getKey)) // Break ties alphabetically
                            .limit(50) // Limit to top 50 words
                            .collect(Collectors.toList());

                    // Send updated stats
                    getSender().tell(new Messages.WordStatsResponse(sortedWordStats), getSelf());
                })
                .match(Messages.GetCumulativeStats.class, request -> {
                    // Log request for cumulative stats
                    System.out.println("WordStatsActor: Received request for cumulative stats.");

                    // Sort cumulative stats by frequency and limit to top 50
                    List<Map.Entry<String, Long>> sortedWordStats = cumulativeWordFrequencies.entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed() // Sort by frequency descending
                                    .thenComparing(Map.Entry::getKey)) // Break ties alphabetically
                            .limit(50) // Limit to top 50 words
                            .collect(Collectors.toList());

                    // Send cumulative stats
                    getSender().tell(new Messages.WordStatsResponse(sortedWordStats), getSelf());
                })
                .matchAny(message -> {
                    // Handle unexpected messages
                    System.err.println("WordStatsActor: Received unexpected message of type " + message.getClass().getName());
                    getSender().tell(new Messages.ErrorMessage("Invalid message type"), getSelf());
                })
                .build();
    }

    /**
     * A basic list of stopwords to exclude from word statistics.
     */
    private static boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "a", "an", "http","https","are", "www", "the", "and", "or", "but", "on", "in", "with", "is", "to", "of", "for", "at", "by", "from", "as", "it", "this", "that", "been"
        );
        return stopWords.contains(word);
    }
}