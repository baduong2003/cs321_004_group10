package book;

public class Chapter {
    private int chapterNumber;
    private String title;
    private String content;

    public Chapter(int chapterNumber, String title) {
        this.chapterNumber = chapterNumber;
        this.title = title;
        this.content = "";
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
        this.chapterNumber = chapterNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }
}