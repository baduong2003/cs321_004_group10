package book;

import java.time.LocalDate;
import java.util.Arrays;

public class Book {
    private String title;
    private String author;
    private String content;
    // How to use date: LocalDate.of(2026, 9, 21)
    private LocalDate publicationDate;
    private String isbn;
    private Chapter[] chapters;

    public Book(String title, String author, LocalDate publicationDate, String isbn) {
        setTitle(title);
        setAuthor(author);
        this.content = "";
        this.publicationDate = publicationDate;
        setIsbn(isbn);
        this.chapters = new Chapter[0];
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * Returns direct book content if set; otherwise combines text from all chapters
     * for the Text Extraction pipeline stage.
     */
    public String getContent() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        if (chapters == null || chapters.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Chapter chapter : chapters) {
            if (chapter != null && chapter.hasContent()) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append("Chapter ").append(chapter.getChapterNumber())
                        .append(": ").append(chapter.getTitle()).append("\n")
                        .append(chapter.getContent());
            }
        }
        return sb.toString();
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public String getIsbn() {
        return isbn;
    }

    public Chapter[] getChapters() {
        return chapters;
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public void setAuthor(String author) {
        this.author = (author != null) ? author.trim() : "";
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setIsbn(String isbn) {
        this.isbn = (isbn != null) ? isbn.trim() : "";
    }

    public void setChapters(Chapter[] chapters) {
        this.chapters = (chapters != null) ? chapters : new Chapter[0];
    }

    /**
     * Helper method to append a new Chapter to the Chapter[] array.
     */
    public void addChapter(Chapter chapter) {
        if (chapter == null) {
            throw new IllegalArgumentException("Cannot add a null chapter.");
        }
        Chapter[] updated = Arrays.copyOf(this.chapters, this.chapters.length + 1);
        updated[this.chapters.length] = chapter;
        this.chapters = updated;
    }

    /**
     * Helper method to retrieve a chapter by its chapter number.
     */
    public Chapter getChapterByNumber(int chapterNumber) {
        for (Chapter chapter : chapters) {
            if (chapter != null && chapter.getChapterNumber() == chapterNumber) {
                return chapter;
            }
        }
        return null;
    }
}