package business;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import bll.EditorBO;
import bll.IEditorBO;
import dal.IFacadeDAO;
import dto.Documents;
import dto.Pages;

import java.io.File;
import java.util.*;

/**
 * JUnit 5 Tests for EditorBO (Business Layer)
 * Tests the Facade pattern and Command-like operations.
 * Uses a mock implementation of IFacadeDAO for swappable testing.
 */
public class EditorBOTest {

    private IEditorBO editorBO;
    private MockFacadeDAO mockDAO;

    /**
     * Mock implementation of IFacadeDAO for testing without database.
     * This makes the tests "swappable" - designed against interfaces.
     */
    static class MockFacadeDAO implements IFacadeDAO {
        boolean createCalled = false;
        boolean updateCalled = false;
        boolean deleteCalled = false;
        String lastFileName = null;
        String lastContent = null;
        List<Documents> mockFiles = new ArrayList<>();

        @Override
        public boolean createFileInDB(String name, String content) {
            createCalled = true;
            lastFileName = name;
            lastContent = content;
            List<Pages> pages = new ArrayList<>();
            pages.add(new Pages(1, 1, 1, content));
            mockFiles.add(new Documents(1, name, "mockHash", "2024-01-01", "2024-01-01", pages));
            return true;
        }

        @Override
        public boolean updateFileInDB(int id, String fileName, int pageNumber, String content) {
            updateCalled = true;
            lastFileName = fileName;
            lastContent = content;
            return true;
        }

        @Override
        public boolean deleteFileInDB(int id) {
            deleteCalled = true;
            return id > 0; // fail for invalid ids
        }

        @Override
        public List<Documents> getFilesFromDB() {
            return mockFiles;
        }

        @Override
        public String transliterateInDB(int pageId, String arabicText) {
            return "transliterated_" + arabicText;
        }

        @Override
        public Map<String, String> lemmatizeWords(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "test_lemma");
            return result;
        }

        @Override
        public Map<String, List<String>> extractPOS(String text) {
            Map<String, List<String>> result = new HashMap<>();
            result.put("test", Arrays.asList("NOUN"));
            return result;
        }

        @Override
        public Map<String, String> extractRoots(String text) {
            Map<String, String> result = new HashMap<>();
            result.put("test", "tst");
            return result;
        }

        @Override
        public double performTFIDF(List<String> unSelectedDocsContent, String selectedDocContent) {
            return 0.5;
        }

        @Override
        public Map<String, Double> performPMI(String content) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Double> performPKL(String content) {
            return new HashMap<>();
        }

        @Override
        public Map<String, String> stemWords(String text) {
            return new HashMap<>();
        }

        @Override
        public Map<String, String> segmentWords(String text) {
            return new HashMap<>();
        }
    }

    @BeforeEach
    public void setUp() {
        mockDAO = new MockFacadeDAO();
        editorBO = new EditorBO(mockDAO);
    }

    @AfterEach
    public void tearDown() {
        editorBO = null;
        mockDAO = null;
    }

    // ==================== CREATE FILE TESTS ====================

    @Test
    @DisplayName("Positive: Create file delegates to DAO successfully")
    public void testCreateFile() {
        boolean result = editorBO.createFile("test.txt", "Hello World");
        assertTrue(result, "createFile should return true on success");
        assertTrue(mockDAO.createCalled, "DAO createFileInDB should be called");
        assertEquals("test.txt", mockDAO.lastFileName);
    }

    @Test
    @DisplayName("Negative: Create file with empty name")
    public void testCreateFileEmptyName() {
        boolean result = editorBO.createFile("", "content");
        // Should still delegate to DAO; the DAO handles validation
        assertTrue(mockDAO.createCalled, "DAO should still be called");
    }

    // ==================== UPDATE FILE TESTS ====================

    @Test
    @DisplayName("Positive: Update file delegates correctly")
    public void testUpdateFile() {
        boolean result = editorBO.updateFile(1, "updated.txt", 1, "new content");
        assertTrue(result, "updateFile should return true");
        assertTrue(mockDAO.updateCalled, "DAO updateFileInDB should be called");
    }

    // ==================== DELETE FILE TESTS ====================

    @Test
    @DisplayName("Positive: Delete file with valid ID")
    public void testDeleteFile() {
        boolean result = editorBO.deleteFile(1);
        assertTrue(result, "deleteFile should return true for valid ID");
        assertTrue(mockDAO.deleteCalled, "DAO deleteFileInDB should be called");
    }

    // ==================== GET FILE EXTENSION TESTS ====================

    @Test
    @DisplayName("Positive: Get extension for .txt file")
    public void testGetFileExtensionTxt() {
        String ext = editorBO.getFileExtension("document.txt");
        assertEquals("txt", ext, "Should return 'txt'");
    }

    @Test
    @DisplayName("Positive: Get extension for .md file")
    public void testGetFileExtensionMd() {
        String ext = editorBO.getFileExtension("readme.md");
        assertEquals("md", ext, "Should return 'md'");
    }

    @Test
    @DisplayName("Negative: Get extension for file with no extension")
    public void testGetFileExtensionNone() {
        String ext = editorBO.getFileExtension("noextension");
        assertEquals("", ext, "Should return empty string for no extension");
    }

    @Test
    @DisplayName("Boundary: Get extension for file with multiple dots")
    public void testGetFileExtensionMultipleDots() {
        String ext = editorBO.getFileExtension("archive.tar.gz");
        assertEquals("gz", ext, "Should return last extension");
    }

    @Test
    @DisplayName("Boundary: Get extension for hidden file")
    public void testGetFileExtensionHiddenFile() {
        String ext = editorBO.getFileExtension(".gitignore");
        assertEquals("gitignore", ext, "Should return 'gitignore'");
    }

    // ==================== IMPORT FILE TESTS ====================

    @Test
    @DisplayName("Positive: Import a valid .txt file")
    public void testImportTxtFile() throws Exception {
        // Create a temp file for testing
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write("test content for import");
        writer.close();

        boolean result = editorBO.importTextFiles(tempFile, "test.txt");
        assertTrue(result, "Importing a .txt file should succeed");
    }

    @Test
    @DisplayName("Negative: Import a non-text file (.jpg) should fail")
    public void testImportNonTextFile() throws Exception {
        File tempFile = File.createTempFile("test", ".jpg");
        tempFile.deleteOnExit();
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write("fake image content");
        writer.close();

        boolean result = editorBO.importTextFiles(tempFile, "test.jpg");
        assertFalse(result, "Importing a .jpg file should fail");
    }

    @Test
    @DisplayName("Negative: Import non-existent file should return false")
    public void testImportNonExistentFile() {
        File fakeFile = new File("non_existent_file_12345.txt");
        boolean result = editorBO.importTextFiles(fakeFile, "fake.txt");
        assertFalse(result, "Importing non-existent file should return false");
    }

    // ==================== GET ALL FILES TESTS ====================

    @Test
    @DisplayName("Positive: Get all files returns list from DAO")
    public void testGetAllFiles() {
        editorBO.createFile("file1.txt", "content1");
        List<Documents> files = editorBO.getAllFiles();
        assertNotNull(files);
        assertFalse(files.isEmpty(), "Should return at least one file");
    }

    @Test
    @DisplayName("Boundary: Get all files when empty returns empty list")
    public void testGetAllFilesEmpty() {
        List<Documents> files = editorBO.getAllFiles();
        assertNotNull(files, "Should return empty list, not null");
        assertTrue(files.isEmpty(), "Should be empty when no files exist");
    }

    // ==================== TRANSLITERATE TESTS ====================

    @Test
    @DisplayName("Positive: Transliterate delegates to DAO")
    public void testTransliterate() {
        String result = editorBO.transliterate(1, "عربي");
        assertNotNull(result, "Transliteration result should not be null");
    }
}
