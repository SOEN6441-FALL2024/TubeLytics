package utils;

import models.Video;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;

/** Helper class to calculate readability scores for text and to calculate sentiment submission
 * @author Deniz Dinchdonmez, Jessica Chen
 * */
public class Helpers {
  private static String[] happyList = {"happy", "wonderful", "great", "lovely", "excited", "yay", "!", "amazing",
          "benefits", "love", "excellent", "good", "laugh", "smile", "thankful", "thanks", "funny", "laugh-out-loud",
          "hilarious", "sweet", ":)", "awesome", "cute", "best", "U+1F600", "U+1F604", "U+1F602", "U+1F606", "U+1F60A",
          "U+1F970", "U+1F61A", "U+263A", "U+1F973"};

  private static String[] sadList = {"sad", "disappointed", "depressed", "upset", "hate", "angry", "frustrated",
          "gloomy", "terrible", "awful", "difficult", ":(", "cry", "death", "murder", "accident", "sickness", "illness", "disease",
          "lost", "loss", "sick", ">:(", "U+1F912", "U+1F61F", "U+1FAE4", "U+1F641", "U+1F621", "U+1F622" };


  /**
   * Private constructor to prevent instantiation of this class
   *
   * @author Deniz Dinchdonmez
   */
  private Helpers() {
    throw new IllegalStateException("private constructor invoked for class: " + getClass());
  }

  public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

  /**
   * Formats a double to two decimal places
   *
   * @param value the double to format
   * @return the formatted double
   * @author Deniz Dinchdonmez
   */
  public static double formatDouble(double value) {
    return Double.parseDouble(decimalFormat.format(value));
  }

  /**
   * Calculates the Flesch-Kincaid Grade Level of a given text
   *
   * @param description the text to analyze
   * @return the Flesch-Kincaid Grade Level
   * @author Deniz Dinchdonmez
   */
  private static TextMetrics calculateTextMetrics(String description) {
    List<String> words = Arrays.asList(description.split("\\s+"));

    long sentenceCount = countSentences(description);
    long wordCount = words.size();
    long syllableCount = words.stream().mapToInt(Helpers::countSyllables).sum();

    return new TextMetrics(sentenceCount, wordCount, syllableCount);
  }

  /**
   * Calculates the Flesch-Kincaid Grade Level of a given text
   *
   * @param description the text to analyze
   * @return the Flesch-Kincaid Grade Level
   * @author Deniz Dinchdonmez
   */
  public static double calculateFleschKincaidGradeLevel(String description) {
    TextMetrics metrics = calculateTextMetrics(description);

    double wordsPerSentence = (double) metrics.wordCount / metrics.sentenceCount;
    double syllablesPerWord = (double) metrics.syllableCount / metrics.wordCount;

    return formatDouble(0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59);
  }

  /**
   * Calculates the Flesch Reading Ease Score of a given text
   *
   * @param description the text to analyze
   * @return the Flesch Reading Ease Score
   * @author Deniz Dinchdonmez
   */
  public static double calculateFleschReadingEaseScore(String description) {
    TextMetrics metrics = calculateTextMetrics(description);

    double wordsPerSentence = (double) metrics.wordCount / metrics.sentenceCount;
    double syllablesPerWord = (double) metrics.syllableCount / metrics.wordCount;

    return formatDouble(206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord);
  }

  /**
   * Counts the number of syllables in a word
   *
   * @param word the word to count syllables in
   * @return the number of syllables in the word
   * @author Deniz Dinchdonmez
   */
  public static int countSyllables(String word) {
    word = word.toLowerCase();
    int count = 0;
    boolean lastWasVowel = false;
    String vowels = "aeiouy";

    for (char c : word.toCharArray()) {
      if (vowels.indexOf(c) != -1) {
        if (!lastWasVowel) {
          count++;
          lastWasVowel = true;
        }
      } else {
        lastWasVowel = false;
      }
    }

    // Adjust syllable count for words that end in "e"
    if (word.endsWith("e") && count > 1) {
      count--;
    }

    return count;
  }

  /**
   * Counts the number of sentences in a text
   *
   * @param description the text to count sentences in
   * @return the number of sentences in the text
   * @author Deniz Dinchdonmez
   */
  public static long countSentences(String description) {
    return Pattern.compile("[.!?]").splitAsStream(description).count();
  }

  /**
   * Helper class to store the metrics of a text
   *
   * @author Deniz Dinchdonmez
   */
  private static class TextMetrics {
    long sentenceCount;
    long wordCount;
    long syllableCount;

    TextMetrics(long sentenceCount, long wordCount, long syllableCount) {
      this.sentenceCount = sentenceCount;
      this.wordCount = wordCount;
      this.syllableCount = syllableCount;
    }
  }

  /**
   * Calculates happy word count in video description
   * @param videoDescription - description of video
   * @return number of matches between happy word list and video description
   * @author Jessica Chen
   */
  public static long calculateHappyWordCount(String videoDescription) {
    long happyWordCount = Arrays.asList(videoDescription.replaceAll("[^a-zA-Z0-9\\s:;()\\-_<>=*!^|\\u1F600-\\u1F64F]+",
                    "").split("\\s+")).stream().map((word) -> (word.toLowerCase()))
            .filter(stream(happyList).toList()::contains).count();

    return happyWordCount;
  }

  /**
   * Calculates sad word count in video description
   * @param videoDescription - description of video
   * @return number of matches between sad word list and video description
   * @author Jessica Chen
   */
  public static long calculateSadWordCount(String videoDescription) {
    long sadWordCount = Arrays.asList(videoDescription.replaceAll("[^a-zA-Z0-9\\s:;()\\-_<>=*!^|\\u1F600-\\u1F64F]",
                    "").split("\\s+")).stream().map((word) -> (word.toLowerCase()))
            .filter(stream(sadList).toList()::contains).count();

    return sadWordCount;
  }

  /**
   * Compares happy word count versus sad word count for each video. Assess whether it is happier, sadder or neutral.
   * @param happyWordCount - number of matches between video description and happy word list
   * @param sadWordCount - number of matches between video description and sad word list
   * @return happy, sad or neutral emoji
   * @author Jessica Chen
   */
  public static String calculateSentiment(double happyWordCount, double sadWordCount) {
    double totalCount = happyWordCount + sadWordCount;
    String sentiment = ":-|";
    // calculate percentage of happy words versus sad words
    if (totalCount > 0) {
      if (happyWordCount/totalCount >= 0.70) {
        sentiment = ":-)";
      } else if (sadWordCount/totalCount >= 0.70) {
        sentiment = ":-(";
      }
    }
    return sentiment;
  }

  /**
   * Evaluates the overall sentiment based on sentiments of each video in the list of video results from a query
   * @param videos list of videos from a query entered by the users
   * @return an emoji indicating whether the overall sentiment is happy, sad or neutral
   * @author Jessica Chen
   */
  public static String calculateOverallSentiment(List<Video> videos) {
    double totalHappyWordCount = videos.stream().limit(50).mapToDouble(Video::getHappyWordCount).sum();
    double totalSadWordCount = videos.stream().limit(50).mapToDouble(Video::getSadWordCount).sum();

    String sentiment = calculateSentiment(totalHappyWordCount, totalSadWordCount);

    return sentiment;
  }
}
