package book;

public class Chapter {
    private int chapterNumber;
    private String title;
    private String content;

    public Chapter(int chapterNumber, String title) {
        this(chapterNumber, title, "");
    }

    // Overloaded constructor so content can be passed directly when parsing
    public Chapter(int chapterNumber, String title, String content) {
        setChapterNumber(chapterNumber);
        setTitle(title);
        setContent(content);
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setChapterNumber(int chapterNumber) {
        if (chapterNumber <= 0) {
            throw new IllegalArgumentException("Chapter number must be greater than 0.");
        }
        this.chapterNumber = chapterNumber;
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public void setContent(String content) {
        // Prevents NullPointerExceptions during text extraction and concept analysis
        this.content = (content != null) ? content : "";
    }

    /**
     * Helper method to check if the chapter has any content.
     */
    public boolean hasContent() {
        return !this.content.trim().isEmpty();
    }
}