package utils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Helper class to calculate readability scores for text author: Deniz Dinchdonmez */
public class Helpers {

  private Helpers() {
    throw new IllegalStateException("private constructor invoked for class: " + getClass());
  }

  public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

  /**
   * Formats a double to two decimal places
   *
   * @param value the double to format
   * @return the formatted double
   */
  public static double formatDouble(double value) {
    return Double.parseDouble(decimalFormat.format(value));
  }

  /**
   * Calculates the Flesch-Kincaid Grade Level of a given text
   *
   * @param description the text to analyze
   * @return the Flesch-Kincaid Grade Level
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
   */
  public static long countSentences(String description) {
    return Pattern.compile("[.!?]").splitAsStream(description).count();
  }

  /** Helper class to store the metrics of a text */
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
}
