package data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dal.HashCalculator;

/**
 * JUnit 5 Tests for HashCalculator (Data Layer)
 * Tests MD5 hashing integrity - editing changes hash, original hash is
 * preserved.
 */
public class HashCalculatorTest {

    private String originalText;
    private String originalHash;

    @BeforeEach
    public void setUp() throws Exception {
        originalText = "بسم الله الرحمن الرحيم";
        originalHash = HashCalculator.calculateHash(originalText);
    }

    @AfterEach
    public void tearDown() {
        originalText = null;
        originalHash = null;
    }

    // ==================== POSITIVE TESTS ====================

    @Test
    @DisplayName("Positive: Hash is generated for valid text")
    public void testHashGenerated() throws Exception {
        String hash = HashCalculator.calculateHash("test content");
        assertNotNull(hash, "Hash should not be null");
        assertFalse(hash.isEmpty(), "Hash should not be empty");
    }

    @Test
    @DisplayName("Positive: Same text produces same hash (deterministic)")
    public void testSameTextSameHash() throws Exception {
        String hash1 = HashCalculator.calculateHash("hello world");
        String hash2 = HashCalculator.calculateHash("hello world");
        assertEquals(hash1, hash2, "Same text should produce identical hashes");
    }

    @Test
    @DisplayName("Positive: Different text produces different hash")
    public void testDifferentTextDifferentHash() throws Exception {
        String hash1 = HashCalculator.calculateHash("original content");
        String hash2 = HashCalculator.calculateHash("modified content");
        assertNotEquals(hash1, hash2, "Different text should produce different hashes");
    }

    @Test
    @DisplayName("Positive: Editing file changes current hash but original hash is preserved")
    public void testEditingChangesHashButOriginalPreserved() throws Exception {
        // Simulate: file imported with original content
        String importHash = HashCalculator.calculateHash(originalText);

        // Simulate: file is edited
        String editedText = originalText + " edited content added";
        String currentHash = HashCalculator.calculateHash(editedText);

        // The current session hash should be different (content changed)
        assertNotEquals(importHash, currentHash,
                "Editing a file should change the current session hash");

        // The original import hash should still be the same value
        // (it was stored at import time and never recalculated)
        assertEquals(originalHash, importHash,
                "Original import hash should remain unchanged after editing");
    }

    @Test
    @DisplayName("Positive: Hash of Arabic text is valid")
    public void testArabicTextHash() throws Exception {
        String hash = HashCalculator.calculateHash("بسم الله الرحمن الرحيم");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        // MD5 hash should be 32 hex characters
        assertEquals(32, hash.length(), "MD5 hash should be 32 characters long");
    }

    @Test
    @DisplayName("Positive: Hash is a valid hex string")
    public void testHashIsHexString() throws Exception {
        String hash = HashCalculator.calculateHash("test");
        assertTrue(hash.matches("[0-9A-F]+"), "Hash should be a valid uppercase hex string");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test
    @DisplayName("Negative: Hash of empty string should still work")
    public void testEmptyStringHash() throws Exception {
        String hash = HashCalculator.calculateHash("");
        assertNotNull(hash, "Hash of empty string should not be null");
        assertEquals(32, hash.length(), "Hash of empty string should still be 32 characters");
    }

    @Test
    @DisplayName("Negative: Hash of special characters")
    public void testSpecialCharactersHash() throws Exception {
        String hash = HashCalculator.calculateHash("!@#$%^&*()_+-=[]{}|;':\",./<>?");
        assertNotNull(hash, "Hash of special characters should not be null");
        assertEquals(32, hash.length(), "Hash should be 32 characters");
    }

    @Test
    @DisplayName("Negative: Null input should throw exception")
    public void testNullInput() {
        assertThrows(Exception.class, () -> {
            HashCalculator.calculateHash(null);
        }, "Null input should throw an exception");
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    @DisplayName("Boundary: Hash of single character")
    public void testSingleCharacterHash() throws Exception {
        String hash = HashCalculator.calculateHash("a");
        assertNotNull(hash);
        assertEquals(32, hash.length(), "Hash of single char should be 32 characters");
    }

    @Test
    @DisplayName("Boundary: Hash of very long string")
    public void testVeryLongStringHash() throws Exception {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longText.append("word ");
        }
        String hash = HashCalculator.calculateHash(longText.toString());
        assertNotNull(hash);
        assertEquals(32, hash.length(), "Hash of long text should still be 32 characters");
    }

    @Test
    @DisplayName("Boundary: Minor change in text produces completely different hash")
    public void testMinorChangeProducesDifferentHash() throws Exception {
        String hash1 = HashCalculator.calculateHash("hello world");
        String hash2 = HashCalculator.calculateHash("hello world!"); // Added just '!'
        assertNotEquals(hash1, hash2,
                "Even a minor change should produce a completely different hash");
    }
}
