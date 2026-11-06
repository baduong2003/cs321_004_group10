package slides;

public class Slide {
    private int slideNumber;
    private String title;
    private String content;

    public Slide(int slideNumber, String title) {
        this(slideNumber, title, "");
    }

    public Slide(int slideNumber, String title, String content) {
        setSlideNumber(slideNumber);
        setTitle(title);
        setContent(content);
    }

    public int getSlideNumber() {
        return slideNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setSlideNumber(int slideNumber) {
        if (slideNumber <= 0) {
            throw new IllegalArgumentException("Slide number must be greater than 0.");
        }
        this.slideNumber = slideNumber;
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public boolean hasContent() {
        return this.content != null && !this.content.trim().isEmpty();
    }
}