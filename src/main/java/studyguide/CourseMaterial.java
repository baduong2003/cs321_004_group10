package studyguide;

import book.Book;
import book.Chapter;

public class CourseMaterial {

    private String title;
    private MaterialType type;
    private String rawText;
    private Book book; // Holds the Book when type == MaterialType.BOOK

    /**
     * Constructor 1: Create CourseMaterial directly from a Book object.
     */
    public CourseMaterial(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null.");
        }
        this.book = book;
        this.title = book.getTitle();
        this.type = MaterialType.BOOK;
        this.rawText = "";
    }

    /**
     * Validate files/materials.
     * Checks if the material actually contains usable text (either in the Book or rawText).
     */
    public boolean isValid() {
        if (type == MaterialType.BOOK) {
            if (book == null || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                return false;
            }
            // Valid if the book has direct content OR at least one chapter with content
            if (book.getContent() != null && !book.getContent().trim().isEmpty()) {
                return true;
            }
            Chapter[] chapters = book.getChapters();
            if (chapters != null) {
                for (Chapter ch : chapters) {
                    if (ch != null && ch.getContent() != null && !ch.getContent().trim().isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }
        return title != null && !title.isEmpty() && rawText != null && !rawText.trim().isEmpty();
    }

    /**
     * Extract text.
     * Pulls the text out of the Book in a single format so the "Find concepts" stage can analyze it.
     */
    public String extractText() {
        if (type == MaterialType.BOOK && book != null) {
            // If Book.content was set directly, return it
            if (book.getContent() != null && !book.getContent().trim().isEmpty()) {
                return book.getContent().trim();
            }
            // Otherwise, combine the content of all Chapters in the Book
            StringBuilder extracted = new StringBuilder();
            Chapter[] chapters = book.getChapters();
            if (chapters != null) {
                for (Chapter ch : chapters) {
                    if (ch != null && ch.getContent() != null && !ch.getContent().trim().isEmpty()) {
                        if (extracted.length() > 0) {
                            extracted.append("\n\n");
                        }
                        extracted.append("Chapter ")
                                .append(ch.getChapterNumber())
                                .append(": ")
                                .append(ch.getTitle())
                                .append("\n")
                                .append(ch.getContent().trim());
                    }
                }
            }
            return extracted.toString();
        }
        return (rawText != null) ? rawText.trim() : "";
    }

    /**
     * Extracts text from only specific chapters of a Book
     */
    public String extractTextForChapters(int[] chapterNumbers) {
        if (type != MaterialType.BOOK || book == null || book.getChapters() == null || chapterNumbers == null) {
            return "";
        }
        StringBuilder extracted = new StringBuilder();
        for (int targetNum : chapterNumbers) {
            for (Chapter ch : book.getChapters()) {
                if (ch != null && ch.getChapterNumber() == targetNum && ch.getContent() != null) {
                    if (extracted.length() > 0) {
                        extracted.append("\n\n");
                    }
                    extracted.append("Chapter ")
                            .append(ch.getChapterNumber())
                            .append(": ")
                            .append(ch.getTitle())
                            .append("\n")
                            .append(ch.getContent().trim());
                }
            }
        }
        return extracted.toString();
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public MaterialType getType() {
        return type;
    }

    public void setType(MaterialType type) {
        this.type = type;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = (rawText != null) ? rawText : "";
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            this.type = MaterialType.BOOK;
            this.title = book.getTitle();
        }
    }
}