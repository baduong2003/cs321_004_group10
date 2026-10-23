package studyguide;

public enum MaterialType {
    BOOK("Digital Textbook"),
    LECTURE_NOTES("Class Notes"),
    LECTURE_SLIDES("Lecture Slides"),
    PRIOR_TEST("Previous Test");

    private final String displayName;

    MaterialType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
