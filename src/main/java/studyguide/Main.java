package studyguide;

import book.Book;
import note.ClassNote;
import slides.LectureSlides;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Study Guide Generator.
 *
 * Load any mix of textbook, lecture slides, class notes and previous tests (PDF).
 * The program finds the important terms across ALL of them, finds a definition for each term
 * in the materials, and turns the result into a vocabulary list, key-concept bullets and a
 * flashcard quiz. Nobody has to type a definition.
 */
public class Main {

    private static final String TEST_FILES_DIR = "test-files";
    private static final Scanner IN = new Scanner(System.in);

    private static int maxTerms = 30;

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("          STUDY GUIDE GENERATOR           ");
        System.out.println("==========================================");
        System.out.println("Load your textbook, notes and slides (PDF) and");
        System.out.println("we'll build the vocabulary and flashcards for you.");

        ConceptIdentifier identifier = new ConceptIdentifier();
        DefinitionMatcher matcher = new DefinitionMatcher();
        BulletPointGenerator bulletGenerator = new BulletPointGenerator(identifier);
        FlashCardGenerator flashCardGenerator = new FlashCardGenerator();
        StudyGuideBuilder builder = new StudyGuideBuilder(identifier, matcher);

        // Optional: java studyguide.Main notes.pdf book.pdf slides.pdf (type guessed from the file name)
        for (String arg : args) {
            addFile(builder, arg, guessType(arg));
        }

        boolean running = true;
        while (running) {
            printMenu(builder, identifier);
            String choice = IN.nextLine().trim();

            switch (choice) {
                case "1":
                    addMaterial(builder);
                    break;
                case "2":
                    manageMaterials(builder);
                    break;
                case "3":
                    if (requireMaterials(builder)) {
                        showVocabulary(builder.build(maxTerms), builder);
                    }
                    break;
                case "4":
                    if (requireMaterials(builder)) {
                        showBullets(builder, bulletGenerator);
                    }
                    break;
                case "5":
                    if (requireMaterials(builder)) {
                        runStudySession(builder, bulletGenerator, flashCardGenerator);
                    }
                    break;
                case "6":
                    changeSettings(identifier);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 0-6.");
            }
        }

        System.out.println("\nGood luck studying!");
    }

    // ---------------------------------------------------------------
    // Menu
    // ---------------------------------------------------------------

    private static void printMenu(StudyGuideBuilder builder, ConceptIdentifier identifier) {
        System.out.println("\n------------------------------------------");
        if (builder.isEmpty()) {
            System.out.println("Loaded materials: none yet");
        } else {
            System.out.println("Loaded materials (" + builder.getMaterials().size() + "):");
            for (CourseMaterial m : builder.getMaterials()) {
                System.out.println("  - " + m.getTitle() + " [" + m.getType().getDisplayName() + "]");
            }
        }
        System.out.println("------------------------------------------");
        System.out.println("1. Add material (PDF)");
        System.out.println("2. Show / remove loaded materials");
        System.out.println("3. Show vocabulary (terms + definitions from all materials)");
        System.out.println("4. Show key-concept bullet points");
        System.out.println("5. Start study session (review, then flashcard quiz)");
        System.out.println("6. Settings (min. term frequency " + identifier.getMinFrequency()
                + ", max terms " + maxTerms + ")");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static boolean requireMaterials(StudyGuideBuilder builder) {
        if (builder.isEmpty()) {
            System.out.println("\nLoad at least one material first (option 1).");
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------

    private static void showVocabulary(List<StudyGuideBuilder.Entry> entries, StudyGuideBuilder builder) {
        if (entries.isEmpty()) {
            printNoDefinitions();
            return;
        }
        System.out.println("\n===== VOCABULARY (" + entries.size() + " terms from "
                + builder.getMaterials().size() + " material(s)) =====");
        for (StudyGuideBuilder.Entry e : entries) {
            System.out.println("\n" + capitalize(e.term));
            System.out.println("   " + e.definition);
            if (builder.getMaterials().size() > 1) {
                System.out.println("   (from: " + e.source + ")");
            }
        }
    }

    private static void showBullets(StudyGuideBuilder builder, BulletPointGenerator generator) {
        List<StudyGuideBuilder.Entry> entries = builder.build(maxTerms);
        if (entries.isEmpty()) {
            printNoDefinitions();
            return;
        }
        List<String> terms = new ArrayList<>();
        for (StudyGuideBuilder.Entry e : entries) {
            terms.add(e.term);
        }
        List<String> bullets = generator.generateBulletPoints(builder.combinedText(), terms);

        System.out.println("\n===== KEY CONCEPTS =====");
        for (String bullet : bullets) {
            System.out.println(cleanBullet(bullet));
        }
    }

    /** Review (vocabulary + bullets) first, then the flashcard quiz. */
    private static void runStudySession(StudyGuideBuilder builder, BulletPointGenerator bulletGenerator,
                                        FlashCardGenerator flashCardGenerator) {
        List<StudyGuideBuilder.Entry> entries = builder.build(maxTerms);
        if (entries.isEmpty()) {
            printNoDefinitions();
            return;
        }

        // Definitions -> flashcards (each card remembers which material the definition came from)
        Map<String, String> definitions = new LinkedHashMap<>();
        Map<String, String> sources = new HashMap<>();
        for (StudyGuideBuilder.Entry e : entries) {
            definitions.put(e.term, e.definition);
            sources.put(e.term, e.source);
        }
        List<Map<String, String>> cards = flashCardGenerator.generateFromDefinitions(definitions, "Combined materials");
        for (Map<String, String> card : cards) {
            String src = sources.get(card.get("topic"));
            if (src != null) {
                card.put("source", src);
            }
        }

        System.out.print("\nReview the vocabulary and key concepts before the quiz? (Y/n): ");
        String answer = IN.nextLine().trim().toLowerCase();
        if (!answer.equals("n") && !answer.equals("no")) {
            showVocabulary(entries, builder);
            System.out.print("\nPress ENTER to see the key concepts...");
            IN.nextLine();
            showBullets(builder, bulletGenerator);
            System.out.print("\nPress ENTER to start the quiz...");
            IN.nextLine();
        }

        flashCardGenerator.displayFlashCards(cards);
    }

    private static void manageMaterials(StudyGuideBuilder builder) {
        if (builder.isEmpty()) {
            System.out.println("\nNo materials loaded yet.");
            return;
        }
        List<CourseMaterial> list = builder.getMaterials();
        System.out.println("\nLoaded materials:");
        for (int i = 0; i < list.size(); i++) {
            CourseMaterial m = list.get(i);
            int words = m.extractText().split("\\s+").length;
            System.out.println("  " + (i + 1) + ". " + m.getTitle() + " [" + m.getType().getDisplayName()
                    + ", ~" + words + " words]");
        }
        System.out.print("Enter a number to remove it, 'c' to clear all, or press ENTER to go back: ");
        String input = IN.nextLine().trim().toLowerCase();
        if (input.equals("c")) {
            builder.clear();
            System.out.println("Cleared.");
            return;
        }
        try {
            int n = Integer.parseInt(input);
            if (n >= 1 && n <= list.size()) {
                CourseMaterial removed = builder.remove(n - 1);
                System.out.println("Removed " + removed.getTitle() + ".");
            }
        } catch (NumberFormatException ignored) {
            // blank or not a number: just go back
        }
    }

    private static void changeSettings(ConceptIdentifier identifier) {
        System.out.println("\n1. Minimum times a word must appear to count as a term (now "
                + identifier.getMinFrequency() + ")");
        System.out.println("2. Maximum number of terms / flashcards (now " + maxTerms + ")");
        System.out.print("Choose (or ENTER to go back): ");
        String choice = IN.nextLine().trim();
        if (!choice.equals("1") && !choice.equals("2")) {
            return;
        }
        System.out.print("New value (1 or higher): ");
        try {
            int value = Integer.parseInt(IN.nextLine().trim());
            if (value < 1) {
                System.out.println("Must be at least 1.");
            } else if (choice.equals("1")) {
                identifier.setMinFrequency(value);
            } else {
                maxTerms = value;
            }
        } catch (NumberFormatException e) {
            System.out.println("That's not a number. Nothing changed.");
        }
    }

    private static void printNoDefinitions() {
        System.out.println("\nNo terms with definitions were found in the loaded materials.");
        System.out.println("Tip: try lowering the minimum term frequency (option 6), or add a textbook/notes");
        System.out.println("that define terms, e.g. \"Term: definition\" or \"A term is ...\".");
    }

    // ---------------------------------------------------------------
    // Loading materials
    // ---------------------------------------------------------------

    private static void addMaterial(StudyGuideBuilder builder) {
        System.out.println("\nWhat kind of material is it?");
        System.out.println("1. Textbook");
        System.out.println("2. Class notes");
        System.out.println("3. Lecture slides");
        System.out.println("4. Previous test");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");

        MaterialType type;
        switch (IN.nextLine().trim()) {
            case "1": type = MaterialType.BOOK; break;
            case "2": type = MaterialType.LECTURE_NOTES; break;
            case "3": type = MaterialType.LECTURE_SLIDES; break;
            case "4": type = MaterialType.PRIOR_TEST; break;
            case "0": return;
            default:
                System.out.println("Invalid option.");
                return;
        }

        String path = askForFile();
        File f = new File(path);

        if (f.isDirectory()) {
            // A folder: add every PDF / .txt inside as this type
            File[] files = f.listFiles();
            int added = 0;
            if (files != null) {
                Arrays.sort(files);
                for (File child : files) {
                    if (isSupported(child.getName()) && addFile(builder, child.getPath(), type)) {
                        added++;
                    }
                }
            }
            if (added == 0) {
                System.out.println("No usable PDF files found in that folder.");
            }
        } else {
            addFile(builder, path, type);
        }
    }

    /** Lists PDF / .txt files in the test-files folder to pick from by number, or lets the user type a path. */
    private static String askForFile() {
        List<String> found = new ArrayList<>();
        File[] files = new File(TEST_FILES_DIR).listFiles();
        if (files != null) {
            Arrays.sort(files);
            for (File f : files) {
                if (f.isFile() && isSupported(f.getName())) {
                    found.add(TEST_FILES_DIR + "/" + f.getName());
                }
            }
        }

        if (!found.isEmpty()) {
            System.out.println("\nFiles in " + TEST_FILES_DIR + "/:");
            for (int i = 0; i < found.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + found.get(i).substring(TEST_FILES_DIR.length() + 1));
            }
            System.out.print("Enter a number, or type the path to a PDF or folder: ");
        } else {
            System.out.print("Enter the path to a PDF or a folder: ");
        }

        String input = IN.nextLine().trim().replaceAll("^\"|\"$", ""); // strips quotes from "Copy as path"
        try {
            int n = Integer.parseInt(input);
            if (n >= 1 && n <= found.size()) {
                return found.get(n - 1);
            }
        } catch (NumberFormatException ignored) {
            // not a number, so treat it as a path
        }
        return input;
    }

    /** Reads one file and adds it to the study guide. Returns true on success. */
    private static boolean addFile(StudyGuideBuilder builder, String path, MaterialType type) {
        if (path == null || !isSupported(path)) {
            System.out.println("Only PDF files are supported: " + path);
            return false;
        }
        if (!java.nio.file.Files.isRegularFile(java.nio.file.Path.of(path))) {
            System.out.println("File not found: " + path);
            return false;
        }

        try {
            String text = new MaterialParser().parse(path);
            String title = fileNameWithoutExtension(path);
            CourseMaterial material;

            switch (type) {
                case BOOK:
                    Book book = new Book(title, "Unknown", LocalDate.now(), "");
                    book.setContent(text);
                    material = new CourseMaterial(book);
                    break;
                case LECTURE_NOTES:
                    material = new CourseMaterial(new ClassNote(title, text));
                    break;
                case LECTURE_SLIDES:
                    material = new CourseMaterial(new LectureSlides(title, text));
                    break;
                default:
                    material = new CourseMaterial(title, MaterialType.PRIOR_TEST, text);
            }

            if (!material.isValid()) {
                System.out.println("No readable text found in " + title + ". "
                        + "(Scanned/image-only PDFs aren't supported - the PDF needs selectable text.)");
                return false;
            }
            builder.add(material);
            System.out.println("Added \"" + title + "\" as " + type.getDisplayName() + ".");
            return true;

        } catch (IOException e) {
            System.out.println("Could not read " + path + " - it may be corrupted or password-protected.");
            return false;
        } catch (java.nio.file.InvalidPathException e) {
            System.out.println("That doesn't look like a valid file path.");
            return false;
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static boolean isSupported(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".pdf"); // PDF only (project constraint)
    }

    /** Guesses the material type from the file name (used for command-line arguments). */
    private static MaterialType guessType(String path) {
        String name = java.nio.file.Path.of(path).getFileName().toString().toLowerCase();
        if (name.contains("slide") || name.contains("lecture") || name.contains("ppt")) {
            return MaterialType.LECTURE_SLIDES;
        }
        if (name.contains("exam") || name.contains("test") || name.contains("quiz")) {
            return MaterialType.PRIOR_TEST;
        }
        if (name.contains("book") || name.contains("chapter") || name.contains("text")) {
            return MaterialType.BOOK;
        }
        return MaterialType.LECTURE_NOTES;
    }

    /**
     * Tidies bullet lines built from "Term: definition" source lines:
     *  - "- Methods: Methods: functions..." becomes "- Methods: functions..."
     */
    private static String cleanBullet(String bullet) {
        String line = bullet.replace("\u2022", "-"); // "bullet" shows as "?" in some Windows consoles
        if (line.startsWith("- ")) {
            String body = line.substring(2);
            int firstColon = body.indexOf(": ");
            if (firstColon > 0) {
                String label = body.substring(0, firstColon);
                String rest = body.substring(firstColon + 2);
                java.util.regex.Matcher m =
                        java.util.regex.Pattern.compile("^([A-Za-z][A-Za-z ]*): (.+)$").matcher(rest);
                if (m.matches() && m.group(1).equalsIgnoreCase(label)) {
                    return "- " + label + ": " + m.group(2);
                }
            }
        }
        return line;
    }

    private static String fileNameWithoutExtension(String path) {
        String name = java.nio.file.Path.of(path).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private static String capitalize(String s) {
        return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}