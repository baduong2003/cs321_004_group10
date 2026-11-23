
package book;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class BookTest {

    private static final Path TEST_FILES_DIR = Paths.get("test-files");
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        runTest("Constructor and getters", BookTest::testConstructorAndGetters);
        runTest("Setters with null values", BookTest::testSettersWithNullValues);
        runTest("Direct content from file", BookTest::testDirectContentFromFile);
        runTest("Combining chapter content", BookTest::testGetContentCombinesChaptersFromFiles);
        runTest("Direct content takes precedence", BookTest::testDirectContentFromBookTxt);
        runTest("Skipping empty or null chapters", BookTest::testGetContentSkipsEmptyOrNullChapters);
        runTest("Adding chapters", BookTest::testAddChapter);
        runTest("Finding chapters by number", BookTest::testGetChapterByNumber);

        System.out.println("\n========== BOOK TEST RESULTS ==========");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("=======================================");
    }

    private static void runTest(String name, TestMethod test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);
        } catch (Exception | AssertionError e) {
            failed++;
            System.out.println("[FAIL] " + name + ": " + e.getMessage());
        }
    }

    private static Book createBook() {
        return new Book(
                "  Effective Testing  ",
                "  Jane Doe  ",
                LocalDate.of(2026, 9, 21),
                "  978-3-16-148410-0  "
        );
    }

    private static String readTestFile(String... subPaths) throws IOException {
        Path filePath = TEST_FILES_DIR;

        for (String part : subPaths) {
            filePath = filePath.resolve(part);
        }

        return Files.readString(filePath, StandardCharsets.UTF_8).trim();
    }

    private static void testConstructorAndGetters() {
        Book book = createBook();
        LocalDate pubDate = LocalDate.of(2026, 9, 21);

        check("Effective Testing".equals(book.getTitle()), "Title was not trimmed.");
        check("Jane Doe".equals(book.getAuthor()), "Author was not trimmed.");
        check(pubDate.equals(book.getPublicationDate()), "Publication date is incorrect.");
        check("978-3-16-148410-0".equals(book.getIsbn()), "ISBN was not trimmed.");
        check("".equals(book.getContent()), "Content should initially be empty.");
        check(book.getChapters() != null, "Chapters should not be null.");
        check(book.getChapters().length == 0, "Chapters should initially be empty.");
    }

    private static void testSettersWithNullValues() {
        Book book = createBook();

        book.setTitle(null);
        book.setAuthor(null);
        book.setIsbn(null);
        book.setContent(null);
        book.setChapters(null);
        book.setPublicationDate(null);

        check("".equals(book.getTitle()), "Null title should become empty.");
        check("".equals(book.getAuthor()), "Null author should become empty.");
        check("".equals(book.getIsbn()), "Null ISBN should become empty.");
        check("".equals(book.getContent()), "Null content should become empty.");
        check(book.getChapters() != null, "Chapters should not be null.");
        check(book.getChapters().length == 0, "Null chapters should become an empty array.");
        check(book.getPublicationDate() == null, "Publication date should remain null.");
    }

    private static void testDirectContentFromFile() throws IOException {
        Book book = createBook();
        String fileContent = readTestFile("sampleNotes.txt");

        book.setContent(fileContent);

        check(fileContent.equals(book.getContent()), "Direct file content does not match.");
    }

    private static void testGetContentCombinesChaptersFromFiles() throws IOException {
        Book book = createBook();

        String ch1Text = readTestFile("chapter1.txt");
        String ch2Text = readTestFile("chapter2.txt");

        Chapter ch1 = new Chapter(1, "The Beginning", ch1Text);
        Chapter ch2 = new Chapter(2, "The Crossing", ch2Text);

        book.addChapter(ch1);
        book.addChapter(ch2);

        String expected = "Chapter 1: The Beginning\n" + ch1Text + "\n\n"
                + "Chapter 2: The Crossing\n" + ch2Text;

        check(expected.equals(book.getContent()), "Chapter content was not combined correctly.");
    }

    private static void testDirectContentFromBookTxt() throws IOException {
        Book book = createBook();
        String fileContent = readTestFile("Book", "Book.txt");

        book.setContent(fileContent);

        check(fileContent.equals(book.getContent()), "Direct content does not match.");
        check(book.getContent().contains("Foundations of Software Testing"),
                "Expected book title was not found in content.");
    }

    private static void testGetContentSkipsEmptyOrNullChapters() throws IOException {
        Book book = createBook();
        String ch1Text = readTestFile("chapter1.txt");

        Chapter validChapter = new Chapter(1, "Valid Chapter", ch1Text);
        Chapter emptyChapter = new Chapter(2, "Empty Chapter", "");

        book.setChapters(new Chapter[]{null, validChapter, emptyChapter});

        String expected = "Chapter 1: Valid Chapter\n" + ch1Text;

        check(expected.equals(book.getContent()),
                "Null or empty chapters were not skipped correctly.");
    }

    private static void testAddChapter() {
        Book book = createBook();
        Chapter ch1 = new Chapter(1, "Intro", "Intro text");

        book.addChapter(ch1);

        check(book.getChapters().length == 1, "Chapter was not added.");
        check(book.getChapters()[0] == ch1, "Added chapter does not match.");

        try {
            book.addChapter(null);
            throw new AssertionError("Expected IllegalArgumentException for null chapter.");
        } catch (IllegalArgumentException e) {
            check("Cannot add a null chapter.".equals(e.getMessage()),
                    "Unexpected exception message.");
        }
    }

    private static void testGetChapterByNumber() {
        Book book = createBook();
        Chapter ch1 = new Chapter(1, "One", "First chapter text");
        Chapter ch2 = new Chapter(2, "Two", "Second chapter text");

        book.addChapter(ch1);
        book.addChapter(ch2);

        check(book.getChapterByNumber(1) == ch1, "Chapter 1 was not found.");
        check(book.getChapterByNumber(2) == ch2, "Chapter 2 was not found.");
        check(book.getChapterByNumber(99) == null, "Missing chapter should return null.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface TestMethod {
        void run() throws Exception;
    }
}
