package business;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import bll.SearchWord;
import dto.Documents;
import dto.Pages;

/**
 * JUnit 5 Tests for SearchWord (Business Layer)
 * Tests the searchKeyword method with positive, negative, and boundary cases.
 */
public class SearchWordTest {

    private List<Documents> testDocs;

    @BeforeEach
    public void setUp() {
        testDocs = new ArrayList<>();

        // Create test document 1 with pages
        List<Pages> pages1 = new ArrayList<>();
        pages1.add(new Pages(1, 1, 1, "hello world this is a test document for searching"));
        pages1.add(new Pages(2, 1, 2, "another page with different content here"));
        Documents doc1 = new Documents(1, "TestDoc1.txt", "hash1", "2024-01-01", "2024-01-01", pages1);

        // Create test document 2 with pages
        List<Pages> pages2 = new ArrayList<>();
        pages2.add(new Pages(3, 2, 1, "software testing is important for quality assurance"));
        Documents doc2 = new Documents(2, "TestDoc2.txt", "hash2", "2024-01-02", "2024-01-02", pages2);

        // Create test document 3 with Arabic content
        List<Pages> pages3 = new ArrayList<>();
        pages3.add(new Pages(4, 3, 1, "بسم الله الرحمن الرحيم"));
        Documents doc3 = new Documents(3, "ArabicDoc.txt", "hash3", "2024-01-03", "2024-01-03", pages3);

        testDocs.add(doc1);
        testDocs.add(doc2);
        testDocs.add(doc3);
    }

    @AfterEach
    public void tearDown() {
        testDocs = null;
    }

    // ==================== POSITIVE TESTS ====================

    @Test
    @DisplayName("Positive: Search for existing keyword returns correct result")
    public void testSearchExistingKeyword() {
        List<String> results = SearchWord.searchKeyword("test", testDocs);
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Results should not be empty for an existing keyword");
        assertTrue(results.get(0).contains("TestDoc1.txt"), "Should find keyword in TestDoc1");
    }

    @Test
    @DisplayName("Positive: Search for keyword in second document")
    public void testSearchKeywordInSecondDoc() {
        List<String> results = SearchWord.searchKeyword("testing", testDocs);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find 'testing' in TestDoc2");
        assertTrue(results.get(0).contains("TestDoc2.txt"), "Should match TestDoc2");
    }

    @Test
    @DisplayName("Positive: Search for keyword that appears with prefix word")
    public void testSearchKeywordWithPrefix() {
        List<String> results = SearchWord.searchKeyword("world", testDocs);
        assertNotNull(results);
        assertFalse(results.isEmpty());
        // "hello" should be the prefix word before "world"
        assertTrue(results.get(0).contains("hello"), "Should include prefix word 'hello'");
    }

    @Test
    @DisplayName("Positive: Search returns result for keyword at start of content")
    public void testSearchKeywordAtStart() {
        List<String> results = SearchWord.searchKeyword("hello", testDocs);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find 'hello' at the start of content");
    }

    @Test
    @DisplayName("Positive: Multiple documents match keyword")
    public void testSearchMultipleResults() {
        // Create documents where keyword appears in multiple
        List<Pages> pagesA = new ArrayList<>();
        pagesA.add(new Pages(10, 10, 1, "unit test is good"));
        Documents docA = new Documents(10, "DocA.txt", "hashA", "2024-01-01", "2024-01-01", pagesA);

        List<Pages> pagesB = new ArrayList<>();
        pagesB.add(new Pages(11, 11, 1, "integration test is better"));
        Documents docB = new Documents(11, "DocB.txt", "hashB", "2024-01-01", "2024-01-01", pagesB);

        List<Documents> multiDocs = new ArrayList<>();
        multiDocs.add(docA);
        multiDocs.add(docB);

        List<String> results = SearchWord.searchKeyword("test", multiDocs);
        assertNotNull(results);
        assertEquals(2, results.size(), "Should find keyword in both documents");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test
    @DisplayName("Negative: Search with keyword shorter than 3 characters throws exception")
    public void testSearchShortKeyword() {
        assertThrows(IllegalArgumentException.class, () -> {
            SearchWord.searchKeyword("hi", testDocs);
        }, "Should throw IllegalArgumentException for keywords shorter than 3 characters");
    }

    @Test
    @DisplayName("Negative: Search with empty keyword throws exception")
    public void testSearchEmptyKeyword() {
        assertThrows(IllegalArgumentException.class, () -> {
            SearchWord.searchKeyword("", testDocs);
        }, "Should throw IllegalArgumentException for empty keyword");
    }

    @Test
    @DisplayName("Negative: Search for non-existing keyword returns empty list")
    public void testSearchNonExistingKeyword() {
        List<String> results = SearchWord.searchKeyword("xyznonexistent", testDocs);
        assertNotNull(results, "Results should not be null even when no match");
        assertTrue(results.isEmpty(), "Results should be empty for non-existing keyword");
    }

    @Test
    @DisplayName("Negative: Search in empty document list returns empty list")
    public void testSearchInEmptyDocList() {
        List<Documents> emptyDocs = new ArrayList<>();
        List<String> results = SearchWord.searchKeyword("test", emptyDocs);
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for empty docs");
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    @DisplayName("Boundary: Search with exactly 3 character keyword (minimum valid)")
    public void testSearchMinimumLengthKeyword() {
        // "for" is 3 characters and exists in the test docs
        List<String> results = SearchWord.searchKeyword("for", testDocs);
        assertNotNull(results, "Should not throw for 3-character keyword");
    }

    @Test
    @DisplayName("Boundary: Search with exactly 2 character keyword (just below minimum)")
    public void testSearchBelowMinimumLengthKeyword() {
        assertThrows(IllegalArgumentException.class, () -> {
            SearchWord.searchKeyword("ab", testDocs);
        }, "Should throw for 2-character keyword");
    }

    @Test
    @DisplayName("Boundary: Search with single character keyword throws exception")
    public void testSearchSingleCharKeyword() {
        assertThrows(IllegalArgumentException.class, () -> {
            SearchWord.searchKeyword("a", testDocs);
        }, "Should throw for 1-character keyword");
    }

    @Test
    @DisplayName("Boundary: Case-insensitive search - BUG: contains() is case-sensitive")
    public void testSearchCaseInsensitive() {
        // BUG FOUND: SearchWord uses pageContent.contains(keyword) which is
        // case-sensitive,
        // but then uses equalsIgnoreCase for word matching. This means uppercase
        // keywords
        // will fail at the contains() check even though equalsIgnoreCase would match.
        // This test documents the bug.
        List<String> results = SearchWord.searchKeyword("HELLO", testDocs);
        assertNotNull(results);
        // Due to the bug, uppercase search returns empty even though "hello" exists
        assertTrue(results.isEmpty(),
                "BUG: contains() is case-sensitive, so HELLO doesn't match hello in content");
    }
}
