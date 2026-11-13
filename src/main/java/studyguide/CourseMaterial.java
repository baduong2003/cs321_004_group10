package studyguide;

import book.Book;
import book.Chapter;
import note.ClassNote;
import slides.Slide;
import slides.LectureSlides;

public class CourseMaterial {

    private String title;
    private MaterialType type;
    private String rawText;
    private Book book; // Holds the Book when type == MaterialType.BOOK
    private ClassNote note; // Holds the Note when type == MaterialType.LECTURE_NOTES
    private LectureSlides slides; // Holds the LectureSlides when type == MaterialType.LECTURE_SLIDES

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
     * Constructor 2: Create CourseMaterial directly from a ClassNote object.
     */
    public CourseMaterial(ClassNote note) {
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null.");
        }
        this.note = note;
        this.title = note.getTitle();
        this.type = MaterialType.LECTURE_NOTES;
        this.rawText = "";
    }

    /**
     * Constructor 3: Create CourseMaterial directly from a LectureSlides object.
     */
    public CourseMaterial(LectureSlides slides) {
        if (slides == null) {
            throw new IllegalArgumentException("LectureSlides cannot be null.");
        }
        this.slides = slides;
        this.title = (slides.getTitle() != null) ? slides.getTitle().trim() : "";
        this.type = MaterialType.LECTURE_SLIDES;
        this.rawText = "";
    }

    /**
     * Constructor 4: Create CourseMaterial from raw text (e.g., for Prior Tests).
     */
    public CourseMaterial(String title, MaterialType type, String rawText) {
        this.title = (title != null) ? title.trim() : "";
        this.type = (type != null) ? type : MaterialType.PRIOR_TEST;
        this.rawText = (rawText != null) ? rawText : "";
    }

    /**
     * Validate files/materials.
     * Checks if the material actually contains usable text (either in the Book or rawText).
     */
    public boolean isValid() {
        if (title == null || title.trim().isEmpty() || type == null) {
            return false;
        }

        switch (type) {
            case BOOK:
                if (book == null || book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                    return false;
                }
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

            case LECTURE_NOTES:
                if (note != null) {
                    return note.getTitle() != null && !note.getTitle().trim().isEmpty()
                            && note.getContent() != null && !note.getContent().trim().isEmpty();
                }
                return rawText != null && !rawText.trim().isEmpty();

            case LECTURE_SLIDES:
                if (slides == null || slides.getTitle() == null || slides.getTitle().trim().isEmpty()) {
                    return rawText != null && !rawText.trim().isEmpty();
                }
                if (slides.getContent() != null && !slides.getContent().trim().isEmpty()) {
                    return true;
                }
                Slide[] slideArray = slides.getSlides();
                if (slideArray != null) {
                    for (Slide sl : slideArray) {
                        if (sl != null && sl.getContent() != null && !sl.getContent().trim().isEmpty()) {
                            return true;
                        }
                    }
                }
                return false;

            default:
                return rawText != null && !rawText.trim().isEmpty();
        }
    }

    /**
     * Extract text.
     * Pulls the text out of the Book in a single format so the "Find concepts" stage can analyze it.
     */
    public String extractText() {
        if (type == MaterialType.BOOK && book != null) {
            if (book.getContent() != null && !book.getContent().trim().isEmpty()) {
                return book.getContent().trim();
            }
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

        if (type == MaterialType.LECTURE_NOTES && note != null) {
            if (note.getContent() != null && !note.getContent().trim().isEmpty()) {
                return note.getContent().trim();
            }
            return "";
        }

        if (type == MaterialType.LECTURE_SLIDES && slides != null) {
            if (slides.getContent() != null && !slides.getContent().trim().isEmpty()) {
                return slides.getContent().trim();
            }
            StringBuilder extracted = new StringBuilder();
            Slide[] slideArray = slides.getSlides();
            if (slideArray != null) {
                for (Slide sl : slideArray) {
                    if (sl != null && sl.getContent() != null && !sl.getContent().trim().isEmpty()) {
                        if (extracted.length() > 0) {
                            extracted.append("\n\n");
                        }
                        extracted.append("Slide ")
                                .append(sl.getSlideNumber())
                                .append(": ")
                                .append(sl.getTitle())
                                .append("\n")
                                .append(sl.getContent().trim());
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
                if (ch != null && ch.getChapterNumber() == targetNum
                        && ch.getContent() != null && !ch.getContent().trim().isEmpty()) {
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

    public String extractTextForSlides(int[] slideNumbers) {
        if (type != MaterialType.LECTURE_SLIDES || slides == null || slides.getSlides() == null || slideNumbers == null) {
            return "";
        }
        StringBuilder extracted = new StringBuilder();
        for (int targetNum : slideNumbers) {
            for (Slide sl : slides.getSlides()) {
                if (sl != null && sl.getSlideNumber() == targetNum
                        && sl.getContent() != null && !sl.getContent().trim().isEmpty()) {
                    if (extracted.length() > 0) {
                        extracted.append("\n\n");
                    }
                    extracted.append("Slide ")
                            .append(sl.getSlideNumber())
                            .append(": ")
                            .append(sl.getTitle())
                            .append("\n")
                            .append(sl.getContent().trim());
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

    public ClassNote getNote() {
        return note;
    }

    public void setNote(ClassNote note) {
        this.note = note;
        if (note != null) {
            this.type = MaterialType.LECTURE_NOTES;
            this.title = (note.getTitle() != null) ? note.getTitle().trim() : "";
            this.book = null;
            this.slides = null;
        }
    }

    public LectureSlides getSlides() {
        return slides;
    }

    public void setSlides(LectureSlides slides) {
        this.slides = slides;
        if (slides != null) {
            this.type = MaterialType.LECTURE_SLIDES;
            this.title = (slides.getTitle() != null) ? slides.getTitle().trim() : "";
            this.book = null;
            this.note = null;
        }
    }
}