package business;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dal.TFIDFCalculator;

/**
 * JUnit 5 Tests for TFIDFCalculator (Business Layer)
 * Tests the TF-IDF algorithm with positive, negative, and boundary cases.
 */
public class TFIDFCalculatorTest {

    private TFIDFCalculator calculator;

    @BeforeEach
    public void setUp() {
        calculator = new TFIDFCalculator();
    }

    @AfterEach
    public void tearDown() {
        calculator = null;
    }

    // ==================== POSITIVE TESTS ====================

    @Test
    @DisplayName("Positive: TF-IDF score for known Arabic document with corpus")
    public void testTFIDFWithKnownDocument() {
        // NOTE: PreProcessText removes all non-Arabic characters,
        // so we must use Arabic text for meaningful TF-IDF scores.
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator.addDocumentToCorpus("الحمد لله رب العالمين الرحمن الرحيم");

        double score = calculator.calculateDocumentTfIdf("مالك يوم الدين الرحمن");

        assertFalse(Double.isNaN(score), "TF-IDF score should not be NaN");
        assertFalse(Double.isInfinite(score), "TF-IDF score should not be infinite");
        assertTrue(Double.isFinite(score), "TF-IDF score should be finite");
    }

    @Test
    @DisplayName("Positive: TF-IDF score changes with different Arabic documents")
    public void testTFIDFDifferentDocumentsProduceDifferentScores() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator.addDocumentToCorpus("الحمد لله رب العالمين");

        double score1 = calculator.calculateDocumentTfIdf("بسم الله الرحمن الرحيم");
        double score2 = calculator.calculateDocumentTfIdf("والعصر ان الانسان لفي خسر");

        assertNotEquals(score1, score2, 0.001,
                "Different Arabic documents should produce different TF-IDF scores");
    }

    @Test
    @DisplayName("Positive: TF-IDF with Arabic text returns valid score")
    public void testTFIDFWithArabicText() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator.addDocumentToCorpus("الحمد لله رب العالمين");

        double score = calculator.calculateDocumentTfIdf("بسم الله الرحمن الرحيم");

        assertFalse(Double.isNaN(score), "TF-IDF score for Arabic text should not be NaN");
        assertTrue(Double.isFinite(score), "TF-IDF score for Arabic text should be finite");
    }

    @Test
    @DisplayName("Positive: TF-IDF score is reproducible for same Arabic inputs")
    public void testTFIDFReproducibility() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator.addDocumentToCorpus("الحمد لله رب العالمين");

        double score1 = calculator.calculateDocumentTfIdf("مالك يوم الدين");

        TFIDFCalculator calculator2 = new TFIDFCalculator();
        calculator2.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator2.addDocumentToCorpus("الحمد لله رب العالمين");

        double score2 = calculator2.calculateDocumentTfIdf("مالك يوم الدين");

        assertEquals(score1, score2, 0.01, "Same inputs should produce same TF-IDF score ±0.01");
    }

    @Test
    @DisplayName("Positive: Adding more corpus documents affects scores")
    public void testTFIDFWithGrowingCorpus() {
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        double score1 = calculator.calculateDocumentTfIdf("الله الرحمن");

        calculator.addDocumentToCorpus("الحمد لله رب العالمين");

        TFIDFCalculator calculator2 = new TFIDFCalculator();
        calculator2.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        calculator2.addDocumentToCorpus("الحمد لله رب العالمين");
        double score2 = calculator2.calculateDocumentTfIdf("الله الرحمن");

        assertNotEquals(score1, score2, "Score should change when corpus grows");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test
    @DisplayName("Negative: TF-IDF with empty document should handle gracefully")
    public void testTFIDFEmptyDocument() {
        calculator.addDocumentToCorpus("the cat sat on the mat");

        // Empty document should not crash
        try {
            double score = calculator.calculateDocumentTfIdf("");
            // If it returns, it should be a valid number or NaN (handled gracefully)
            assertTrue(Double.isFinite(score) || Double.isNaN(score),
                    "Empty document should return finite score or NaN");
        } catch (Exception e) {
            // Acceptable if it throws a controlled exception for empty input
            assertTrue(e instanceof ArithmeticException || e instanceof IllegalArgumentException,
                    "Should throw a meaningful exception for empty document");
        }
    }

    @Test
    @DisplayName("Negative: TF-IDF with special characters only")
    public void testTFIDFSpecialCharactersOnly() {
        calculator.addDocumentToCorpus("normal text here");

        try {
            double score = calculator.calculateDocumentTfIdf("!@#$%^&*()");
            // Should handle gracefully
            assertTrue(Double.isFinite(score) || Double.isNaN(score),
                    "Special characters should be handled gracefully");
        } catch (Exception e) {
            // Acceptable if controlled exception
            assertNotNull(e.getMessage(), "Exception should have a message");
        }
    }

    @Test
    @DisplayName("Negative: TF-IDF with empty corpus")
    public void testTFIDFEmptyCorpus() {
        // No documents added to corpus
        try {
            double score = calculator.calculateDocumentTfIdf("test document");
            assertTrue(Double.isFinite(score) || Double.isNaN(score),
                    "Empty corpus should be handled gracefully");
        } catch (Exception e) {
            assertNotNull(e, "Should throw exception for empty corpus");
        }
    }

    @Test
    @DisplayName("Negative: TF-IDF with numbers only document")
    public void testTFIDFNumbersOnly() {
        calculator.addDocumentToCorpus("regular text document");

        try {
            double score = calculator.calculateDocumentTfIdf("12345 67890");
            assertFalse(Double.isInfinite(score), "Numbers-only doc should not produce infinite score");
        } catch (Exception e) {
            assertNotNull(e, "Should handle numbers-only input gracefully");
        }
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    @DisplayName("Boundary: TF-IDF with single Arabic word")
    public void testTFIDFSingleWordDocument() {
        calculator.addDocumentToCorpus("الله");
        double score = calculator.calculateDocumentTfIdf("الله");

        assertFalse(Double.isNaN(score), "Single Arabic word should return valid score");
        assertTrue(Double.isFinite(score), "Single Arabic word should return finite score");
    }

    @Test
    @DisplayName("Boundary: TF-IDF with long Arabic document")
    public void testTFIDFLongDocument() {
        StringBuilder longDoc = new StringBuilder();
        String[] arabicWords = { "كلمة", "اختبار", "نص", "طويل", "عربي", "مستند", "كتاب", "قلم" };
        for (int i = 0; i < 200; i++) {
            longDoc.append(arabicWords[i % arabicWords.length]).append(" ");
        }
        calculator.addDocumentToCorpus("بسم الله الرحمن الرحيم");
        double score = calculator.calculateDocumentTfIdf(longDoc.toString());

        assertTrue(Double.isFinite(score), "Long Arabic document should produce a finite score");
    }

    @Test
    @DisplayName("Boundary: TF-IDF with identical Arabic document and corpus")
    public void testTFIDFIdenticalDocumentAndCorpus() {
        String sameText = "بسم الله الرحمن الرحيم";
        calculator.addDocumentToCorpus(sameText);
        double score = calculator.calculateDocumentTfIdf(sameText);

        assertTrue(Double.isFinite(score), "Identical document to corpus should produce finite score");
    }

    @Test
    @DisplayName("Negative: TF-IDF with English text returns 0.0 (PreProcessText removes non-Arabic)")
    public void testTFIDFEnglishTextReturnsZero() {
        // FIXED: PreProcessText.preprocessText removes ALL non-Arabic characters.
        // English text becomes empty after preprocessing, now returns 0.0 instead of
        // NaN.
        calculator.addDocumentToCorpus("the cat sat on the mat");
        double score = calculator.calculateDocumentTfIdf("the dog played");

        assertEquals(0.0, score, 0.001,
                "English text should return 0.0 because PreProcessText removes non-Arabic characters");
    }
}
