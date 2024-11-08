package utils;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestResult;
import models.SearchResult;
import models.Video;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

/** Tests for the helper methods in the Helpers class. author: Deniz Dinchdonmez , Jessica Chen */
public class HelpersTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  /**
   * Tests the private constructor of the Helpers class. The constructor should throw an
   * IllegalStateException when invoked.
   *
   * @author Deniz Dinchdonmez
   */
  @Test
  public void testPrivateConstructor() throws Exception {
    // Access the private constructor of Helpers
    Constructor<Helpers> constructor = Helpers.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Assert that the constructor throws an IllegalStateException when invoked
    assertThrows(InvocationTargetException.class, constructor::newInstance);

    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      // Ensure the cause is an IllegalStateException
      assertEquals(IllegalStateException.class, e.getCause().getClass());
      assertEquals(
          "private constructor invoked for class: class utils.Helpers", e.getCause().getMessage());
    }
  }

  /**
   * Tests the calculation of the Flesch-Kincaid Grade Level for a given text. the grade level is
   * taken from <a
   * href="https://goodcalculators.com/flesch-kincaid-calculator/">flesch-kincaid-calculator</a> as
   * a reference
   *
   * @author Deniz Dinchdonmez
   */
  @Test
  public void testCalculateFleschKincaidGradeLevel() {
    String description1 = "This is a simple sentence.";
    String description2 = "The quick brown fox jumps over the lazy dog.";

    // Expected values may vary depending on the rounding method; adjust as necessary.
    assertEquals(2.9, Helpers.calculateFleschKincaidGradeLevel(description1), 0.3);
    assertEquals(2.1, Helpers.calculateFleschKincaidGradeLevel(description2), 0.3);
  }

  /**
   * Tests the calculation of the Flesch Reading Ease Score for a given text. the score is taken
   * from <a
   * href="https://goodcalculators.com/flesch-kincaid-calculator/">flesch-kincaid-calculator</a> as
   * a reference
   *
   * @author Deniz Dinchdonmez
   */
  @Test
  public void testCalculateFleschReadingEaseScore() {
    String description1 = "This is a simple sentence.";
    String description2 = "The quick brown fox jumps over the lazy dog.";

    // Expected values may vary depending on the rounding method; adjust as necessary.
    assertEquals(83.3, Helpers.calculateFleschReadingEaseScore(description1), 2);
    assertEquals(96.2, Helpers.calculateFleschReadingEaseScore(description2), 2);
  }

  /**
   * Tests the counting of syllables in a given word. The method should return the number of
   * syllables in the word.
   *
   * @author Deniz Dinchdonmez
   */
  @Test
  public void testCountSyllables() {
    // Testing the syllable counting directly for accuracy
    assertEquals(1, Helpers.countSyllables("simple"));
    assertEquals(1, Helpers.countSyllables("fox"));
    assertEquals(1, Helpers.countSyllables("jumps"));
    assertEquals(1, Helpers.countSyllables("dog"));
  }

  /**
   * Tests the counting of sentences in a given text. The method should return the number of
   * sentences in the text. author: Deniz Dinchdonmez
   */
  @Test
  public void testCountSentences() {
    String text1 = "Hello! How are you? I hope you're doing well.";
    String text2 = "This is a single sentence.";

    assertEquals(3, Helpers.countSentences(text1));
    assertEquals(1, Helpers.countSentences(text2));
  }

  /**
   * Tests the accuracy in counting the number of happy matches between the video description and the happy word list
   * @author Jessica Chen
   */
  @Test
  public void calculateHappyWordCountTest() {
    String description1 = "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.";
    String description2 = "Today is a terrible day with awful weather. I am angry and not happy. This is a sad sentence.";
    String description3 = "Today is a good day but with awful weather. I am happy but also sad. This is a neutral sentence.";
    String description4 = "This is a test sentence with no words matching the predetermined list. This is a neutral sentence.";

    assertEquals(4, Helpers.calculateHappyWordCount(description1));
    assertEquals(1, Helpers.calculateHappyWordCount(description2));
    assertEquals(2, Helpers.calculateHappyWordCount(description3));
    assertEquals(0, Helpers.calculateHappyWordCount(description4));
  }

  /**
   * Tests the accuracy in counting the number of sad matches between the video description and the sad word list
   * @author Jessica Chen
   */
  @Test
  public void calculateSadWordCountTest() {
    String description1 = "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.";
    String description2 = "Today is a terrible day with awful weather. I am angry and not happy. This is a sad sentence.";
    String description3 = "Today is a good day but with awful weather. I am happy but also sad. This is a neutral sentence.";
    String description4 = "This is a test sentence with no words matching the predetermined list. This is a neutral sentence.";

    assertEquals(1, Helpers.calculateSadWordCount(description1));
    assertEquals(4, Helpers.calculateSadWordCount(description2));
    assertEquals(2, Helpers.calculateSadWordCount(description3));
    assertEquals(0, Helpers.calculateSadWordCount(description4));
  }

  /**
   * Tests the sentiment calculation when given the number of happy word counts and the number of sad word counts
   * @author Jessica Chen
   */
  @Test
  public void calculateSentimentTest() {
    String description1 = "Today is a great day with amazing weather. I am very happy and not sad at all. This is a happy sentence.";
    String description2 = "Today is a terrible day with awful weather. I am angry and not happy. This is a sad sentence.";
    String description3 = "Today is a good day but with awful weather. I am happy but also sad. This is a neutral sentence.";
    String description4 = "This is a test sentence with no words matching the predetermined list. This is a neutral sentence.";

    long happyWordCount1 = Helpers.calculateHappyWordCount(description1);
    long sadWordCount1 = Helpers.calculateSadWordCount(description1);

    long happyWordCount2 = Helpers.calculateHappyWordCount(description2);
    long sadWordCount2 = Helpers.calculateSadWordCount(description2);

    long happyWordCount3 = Helpers.calculateHappyWordCount(description3);
    long sadWordCount3 = Helpers.calculateSadWordCount(description3);

    long happyWordCount4 = Helpers.calculateHappyWordCount(description4);
    long sadWordCount4 = Helpers.calculateSadWordCount(description4);


    assertEquals(":-)", Helpers.calculateSentiment(happyWordCount1, sadWordCount1));
    assertEquals(":-(", Helpers.calculateSentiment(happyWordCount2, sadWordCount2));
    assertEquals(":-|", Helpers.calculateSentiment(happyWordCount3, sadWordCount3));
    assertEquals(":-|", Helpers.calculateSentiment(happyWordCount4, sadWordCount4));
  }
}
