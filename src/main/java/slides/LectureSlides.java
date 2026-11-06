package slides;

import java.time.LocalDate;
import java.util.Arrays;

public class LectureSlides {
    private String title;
    private String instructor;
    private String courseName;
    private LocalDate lectureDate;
    private String content;
    private Slide[] slides;

    public LectureSlides(String title, String instructor, String courseName, LocalDate lectureDate) {
        setTitle(title);
        setInstructor(instructor);
        setCourseName(courseName);
        this.lectureDate = lectureDate;
        this.content = "";
        this.slides = new Slide[0];
    }

    public LectureSlides(String title, String content) {
        this(title, "", "", LocalDate.now());
        setContent(content);
    }

    public String getTitle() {
        return title;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getCourseName() {
        return courseName;
    }

    public LocalDate getLectureDate() {
        return lectureDate;
    }

    public String getContent() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        if (slides == null || slides.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Slide slide : slides) {
            if (slide != null && slide.hasContent()) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append("Slide ").append(slide.getSlideNumber())
                        .append(": ").append(slide.getTitle()).append("\n")
                        .append(slide.getContent().trim());
            }
        }
        return sb.toString();
    }

    public Slide[] getSlides() {
        return slides;
    }

    public int getSlideCount() {
        return (slides != null) ? slides.length : 0;
    }

    public void setTitle(String title) {
        this.title = (title != null) ? title.trim() : "";
    }

    public void setInstructor(String instructor) {
        this.instructor = (instructor != null) ? instructor.trim() : "";
    }

    public void setCourseName(String courseName) {
        this.courseName = (courseName != null) ? courseName.trim() : "";
    }

    public void setLectureDate(LocalDate lectureDate) {
        this.lectureDate = lectureDate;
    }

    public void setContent(String content) {
        this.content = (content != null) ? content : "";
    }

    public void setSlides(Slide[] slides) {
        this.slides = (slides != null) ? slides : new Slide[0];
    }

    /**
     * Adds a new Slide to the array.
     * @param slide
     */
    public void addSlide(Slide slide) {
        if (slide == null) {
            throw new IllegalArgumentException("Cannot add a null slide.");
        }
        Slide[] updated = Arrays.copyOf(this.slides, this.slides.length + 1);
        updated[this.slides.length] = slide;
        this.slides = updated;
    }
}
