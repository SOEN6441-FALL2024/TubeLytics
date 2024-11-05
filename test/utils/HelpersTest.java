package utils;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

/** Tests for the helper methods in the Helpers class. author: Deniz Dinchdonmez */
public class HelpersTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

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
   */
  @Test
  public void testCalculateFleschReadingEaseScore() {
    String description1 = "This is a simple sentence.";
    String description2 = "The quick brown fox jumps over the lazy dog.";

    // Expected values may vary depending on the rounding method; adjust as necessary.
    assertEquals(83.3, Helpers.calculateFleschReadingEaseScore(description1), 2);
    assertEquals(96.2, Helpers.calculateFleschReadingEaseScore(description2), 2);
  }

  @Test
  public void testCountSyllables() {
    // Testing the syllable counting directly for accuracy
    assertEquals(1, Helpers.countSyllables("simple"));
    assertEquals(1, Helpers.countSyllables("fox"));
    assertEquals(1, Helpers.countSyllables("jumps"));
    assertEquals(1, Helpers.countSyllables("dog"));
  }

  @Test
  public void testCountSentences() {
    String text1 = "Hello! How are you? I hope you're doing well.";
    String text2 = "This is a single sentence.";

    assertEquals(3, Helpers.countSentences(text1));
    assertEquals(1, Helpers.countSentences(text2));
  }
}
