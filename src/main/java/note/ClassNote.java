package note;

import java.time.LocalDate;

/**
 * Represents a student's class notes uploaded as course material.
 */
public class ClassNote {
    private String title;
    private String courseName;
    private String author;
    // Example usage: LocalDate.of(2026, 10, 23)
    private LocalDate noteDate;
    private String topic;
    private String content;

    public ClassNote(String title, String courseName, String author, LocalDate noteDate, String topic) {
        setTitle(title);
        setCourseName(courseName);
        setAuthor(author);
        this.noteDate = noteDate;
        setTopic(topic);
        this.content = "";
    }

    public ClassNote(String title, String content) {
        this(title, "", "", LocalDate.now(), "");
        setContent(content);
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getNoteDate() {
        return noteDate;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }

    // Setters
    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public void setCourseName(String courseName) {
        this.courseName = (courseName != null) ? courseName.trim() : "";
    }

    public void setAuthor(String author) {
        this.author = (author != null) ? author.trim() : "";
    }

    public void setNoteDate(LocalDate noteDate) {
        this.noteDate = noteDate;
    }

    public void setTopic(String topic) {
        this.topic = (topic != null) ? topic.trim() : "";
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    /**
     * Helper to check if the note has usable text.
     */
    public boolean hasContent() {
        return this.content != null && !this.content.trim().isEmpty();
    }

    /**
     * Helper to return formatted note text.
     */
    public String extractText() {
        if (!hasContent()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!title.isEmpty()) {
            sb.append("Notes: ").append(title);
            if (!topic.isEmpty()) {
                sb.append(" (Topic: ").append(topic).append(")");
            }
            sb.append("\n");
        }
        sb.append(content.trim());
        return sb.toString();
    }
}