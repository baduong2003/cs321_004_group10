package book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private static final Path TEST_FILES_DIR = Paths.get("test-files");
    private Book book;
    private LocalDate pubDate;

    @BeforeEach
    void setUp() {
        pubDate = LocalDate.of(2026, 9, 21);
        book = new Book("  Effective Testing  ", "  Jane Doe  ", pubDate, "  978-3-16-148410-0  ");
    }

    /**
     * Helper method to read text files from src/test-files
     */
    private String readTestFile(String... subPaths) throws IOException {
        Path filePath = TEST_FILES_DIR;
        for (String part : subPaths) {
            filePath = filePath.resolve(part);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8).trim();
    }

    @Test
    @DisplayName("Constructor initializes fields and trims strings")
    void testConstructorAndGetters() {
        assertEquals("Effective Testing", book.getTitle());
        assertEquals("Jane Doe", book.getAuthor());
        assertEquals(pubDate, book.getPublicationDate());
        assertEquals("978-3-16-148410-0", book.getIsbn());
        assertEquals("", book.getContent());
        assertNotNull(book.getChapters());
        assertEquals(0, book.getChapters().length);
    }

    @Test
    @DisplayName("Setters handle null inputs safely by defaulting to empty strings or empty arrays")
    void testSettersWithNullValues() {
        book.setTitle(null);
        book.setAuthor(null);
        book.setIsbn(null);
        book.setContent(null);
        book.setChapters(null);
        book.setPublicationDate(null);

        assertEquals("", book.getTitle());
        assertEquals("", book.getAuthor());
        assertEquals("", book.getIsbn());
        assertEquals("", book.getContent());
        assertNotNull(book.getChapters());
        assertEquals(0, book.getChapters().length);
        assertNull(book.getPublicationDate());
    }

    @Test
    @DisplayName("getContent returns direct content loaded from src/test-files/sample-book-content.txt")
    void testDirectContentFromFile() throws IOException {
        String fileContent = readTestFile("sampleNotes.txt");
        book.setContent(fileContent);

        assertEquals(fileContent, book.getContent());
    }

    @Test
    @DisplayName("getContent combines chapter texts loaded from src/test-files when direct content is empty")
    void testGetContentCombinesChaptersFromFiles() throws IOException {
        String ch1Text = readTestFile("chapter1.txt");
        String ch2Text = readTestFile("chapter2.txt");

        Chapter ch1 = new Chapter(1, "The Beginning", ch1Text);
        Chapter ch2 = new Chapter(2, "The Crossing", ch2Text);

        book.addChapter(ch1);
        book.addChapter(ch2);

        String expected = "Chapter 1: The Beginning\n" + ch1Text + "\n\n"
                + "Chapter 2: The Crossing\n" + ch2Text;

        assertEquals(expected, book.getContent());
    }

    @Test
    @DisplayName("Direct content takes precedence over chapter content")
    void testDirectContentFromBookTxt() throws IOException {
        String fileContent = readTestFile("Book", "Book.txt");
        book.setContent(fileContent);

        assertEquals(fileContent, book.getContent());
        assertTrue(book.getContent().contains("Foundations of Software Testing"));
    }

    @Test
    @DisplayName("getContent skips null chapters or chapters without content")
    void testGetContentSkipsEmptyOrNullChapters() throws IOException {
        String ch1Text = readTestFile("chapter1.txt");

        Chapter validChapter = new Chapter(1, "Valid Chapter", ch1Text);
        Chapter emptyChapter = new Chapter(2, "Empty Chapter", "");

        book.setChapters(new Chapter[]{null, validChapter, emptyChapter});

        String expected = "Chapter 1: Valid Chapter\n" + ch1Text;
        assertEquals(expected, book.getContent());
    }

    @Test
    @DisplayName("addChapter appends chapters and throws IllegalArgumentException on null")
    void testAddChapter() {
        Chapter ch1 = new Chapter(1, "Intro", "Intro text");
        book.addChapter(ch1);

        assertEquals(1, book.getChapters().length);
        assertEquals(ch1, book.getChapters()[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> book.addChapter(null)
        );
        assertEquals("Cannot add a null chapter.", exception.getMessage());
    }

    @Test
    @DisplayName("getChapterByNumber finds matching chapter or returns null if missing")
    void testGetChapterByNumber() {
        Chapter ch1 = new Chapter(1, "One", "First chapter text");
        Chapter ch2 = new Chapter(2, "Two", "Second chapter text");

        book.addChapter(ch1);
        book.addChapter(ch2);

        assertEquals(ch1, book.getChapterByNumber(1));
        assertEquals(ch2, book.getChapterByNumber(2));
        assertNull(book.getChapterByNumber(99));
    }
}
