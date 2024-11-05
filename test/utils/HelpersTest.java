package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

public class HelpersTest extends WithApplication {

  @Override
  protected Application provideApplication() {
    // Building the application using Guice
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testCalculateFleschKincaidGradeLevel() {
    String description1 = "This is a simple sentence.";
    String description2 = "The quick brown fox jumps over the lazy dog.";

    // Expected values may vary depending on the rounding method; adjust as necessary.
    assertEquals(2.9, Helpers.calculateFleschKincaidGradeLevel(description1), 0.3);
    assertEquals(2.1, Helpers.calculateFleschKincaidGradeLevel(description2), 0.3);
  }

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
