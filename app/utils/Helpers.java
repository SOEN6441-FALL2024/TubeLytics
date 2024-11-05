package utils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Helpers {

    public static DecimalFormat DF = new DecimalFormat("#.##");

    public static double formatDouble(double value) {
        return Double.parseDouble(DF.format(value));
    }

    // Helper method to calculate counts needed for readability formulas
    private static TextMetrics calculateTextMetrics(String description) {
        List<String> words = Arrays.asList(description.split("\\s+"));

        long sentenceCount = countSentences(description);
        long wordCount = words.size();
        long syllableCount = words.stream()
                .mapToInt(Helpers::countSyllables)
                .sum();

        return new TextMetrics(sentenceCount, wordCount, syllableCount);
    }

    public static double calculateFleschKincaidGradeLevel(String description) {
        TextMetrics metrics = calculateTextMetrics(description);

        double wordsPerSentence = (double) metrics.wordCount / metrics.sentenceCount;
        double syllablesPerWord = (double) metrics.syllableCount / metrics.wordCount;

        return formatDouble(0.39 * wordsPerSentence + 11.8 * syllablesPerWord - 15.59);
    }

    public static double calculateFleschReadingEaseScore(String description) {
        TextMetrics metrics = calculateTextMetrics(description);

        double wordsPerSentence = (double) metrics.wordCount / metrics.sentenceCount;
        double syllablesPerWord = (double) metrics.syllableCount / metrics.wordCount;

        return formatDouble(206.835 - 1.015 * wordsPerSentence - 84.6 * syllablesPerWord);
    }

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

    public static long countSentences(String description) {
        return Pattern.compile("[.!?]").splitAsStream(description).count();
    }

    // helper Inner class to hold text metrics for readability calculations
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
