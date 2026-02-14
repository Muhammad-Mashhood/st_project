package data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import dal.PaginationDAO;
import dto.Pages;

/**
 * JUnit 5 Tests for PaginationDAO (Data Layer)
 * Tests pagination logic that splits content into pages.
 * Note: PaginationDAO.paginate is package-private, so this test uses reflection
 * or tests through the public API. We test via reflection since it's in a
 * different package.
 */
public class PaginationDAOTest {

    private java.lang.reflect.Method paginateMethod;

    @BeforeEach
    public void setUp() throws Exception {
        // PaginationDAO.paginate is package-private, so we use reflection to access it
        paginateMethod = PaginationDAO.class.getDeclaredMethod("paginate", String.class);
        paginateMethod.setAccessible(true);
    }

    @AfterEach
    public void tearDown() {
        paginateMethod = null;
    }

    @SuppressWarnings("unchecked")
    private List<Pages> callPaginate(String content) throws Exception {
        return (List<Pages>) paginateMethod.invoke(null, content);
    }

    // ==================== POSITIVE TESTS ====================

    @Test
    @DisplayName("Positive: Short content fits on single page")
    public void testShortContentSinglePage() throws Exception {
        String content = "Hello world";
        List<Pages> pages = callPaginate(content);

        assertNotNull(pages, "Pages list should not be null");
        assertEquals(1, pages.size(), "Short content should fit on 1 page");
        assertEquals(content, pages.get(0).getPageContent(), "Page content should match input");
        assertEquals(1, pages.get(0).getPageNumber(), "First page should be numbered 1");
    }

    @Test
    @DisplayName("Positive: Content exactly at page size creates one page")
    public void testExactPageSizeContent() throws Exception {
        // Page size is 100 characters
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            content.append("a");
        }
        List<Pages> pages = callPaginate(content.toString());

        assertNotNull(pages);
        assertEquals(1, pages.size(), "Content exactly at page size should be 1 page");
        assertEquals(100, pages.get(0).getPageContent().length());
    }

    @Test
    @DisplayName("Positive: Content exceeding page size creates multiple pages")
    public void testMultiplePages() throws Exception {
        // Create content that's 250 characters (should be 3 pages: 100+100+50)
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            content.append("x");
        }
        List<Pages> pages = callPaginate(content.toString());

        assertNotNull(pages);
        assertEquals(3, pages.size(), "250 chars should create 3 pages (100+100+50)");
        assertEquals(100, pages.get(0).getPageContent().length(), "First page should be 100 chars");
        assertEquals(100, pages.get(1).getPageContent().length(), "Second page should be 100 chars");
        assertEquals(50, pages.get(2).getPageContent().length(), "Third page should be 50 chars");
    }

    @Test
    @DisplayName("Positive: Page numbers are sequential starting from 1")
    public void testPageNumbersSequential() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 350; i++) {
            content.append("y");
        }
        List<Pages> pages = callPaginate(content.toString());

        for (int i = 0; i < pages.size(); i++) {
            assertEquals(i + 1, pages.get(i).getPageNumber(),
                    "Page " + (i + 1) + " should have correct page number");
        }
    }

    @Test
    @DisplayName("Positive: Arabic text pagination works correctly")
    public void testArabicTextPagination() throws Exception {
        // Arabic characters are multi-byte but Java chars, pagination is char-based
        StringBuilder arabicContent = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            arabicContent.append("ا"); // Arabic letter Alef
        }
        List<Pages> pages = callPaginate(arabicContent.toString());

        assertNotNull(pages);
        assertEquals(2, pages.size(), "150 Arabic chars should create 2 pages");
    }

    // ==================== NEGATIVE TESTS ====================

    @Test
    @DisplayName("Negative: Null content returns a page with empty content")
    public void testNullContent() throws Exception {
        List<Pages> pages = callPaginate(null);

        assertNotNull(pages, "Should return a list even for null content");
        assertFalse(pages.isEmpty(), "Should return at least one page for null content");
    }

    @Test
    @DisplayName("Negative: Empty string content returns a page with empty content")
    public void testEmptyContent() throws Exception {
        List<Pages> pages = callPaginate("");

        assertNotNull(pages, "Should return a list even for empty content");
        assertFalse(pages.isEmpty(), "Should return at least one page for empty content");
        assertEquals("", pages.get(0).getPageContent(), "Empty content should produce empty page");
    }

    // ==================== BOUNDARY TESTS ====================

    @Test
    @DisplayName("Boundary: Content of 1 character creates 1 page")
    public void testSingleCharacter() throws Exception {
        List<Pages> pages = callPaginate("z");

        assertEquals(1, pages.size(), "Single char should create 1 page");
        assertEquals("z", pages.get(0).getPageContent());
    }

    @Test
    @DisplayName("Boundary: Content of 99 characters (just under page size)")
    public void testJustUnderPageSize() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 99; i++) {
            content.append("b");
        }
        List<Pages> pages = callPaginate(content.toString());

        assertEquals(1, pages.size(), "99 chars should fit on 1 page");
    }

    @Test
    @DisplayName("Boundary: Content of 101 characters (just over page size)")
    public void testJustOverPageSize() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            content.append("c");
        }
        List<Pages> pages = callPaginate(content.toString());

        assertEquals(2, pages.size(), "101 chars should create 2 pages");
        assertEquals(100, pages.get(0).getPageContent().length(), "First page should be full (100)");
        assertEquals(1, pages.get(1).getPageContent().length(), "Second page should have 1 char");
    }

    @Test
    @DisplayName("Boundary: Exactly 200 characters creates 2 equal pages")
    public void testExactlyDoublePageSize() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            content.append("d");
        }
        List<Pages> pages = callPaginate(content.toString());

        assertEquals(2, pages.size(), "200 chars should create exactly 2 pages");
        assertEquals(100, pages.get(0).getPageContent().length());
        assertEquals(100, pages.get(1).getPageContent().length());
    }
}
