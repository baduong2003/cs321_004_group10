package book;

import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String content;
    // How to use date: LocalDate.of(2026, 9, 21)
    private LocalDate publicationDate;
    private String isbn;
    private Chapter[] chapters;

    public Book(String title, String author,
                LocalDate publicationDate, String isbn) {
        this.title = title;
        this.author = author;
        this.publicationDate = publicationDate;
        this.isbn = isbn;
        this.chapters = new Chapter[0];
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
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
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setChapters(Chapter[] chapters) {
        this.chapters = chapters;
    }
}