package studyguide;

import book.Book;
import note.ClassNote;
import slides.LectureSlides;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Study Guide Generator - console demo (MVP).
 *
 * Flow: load material -> identify concepts -> match definitions ->
 * show vocabulary list / bullet points / interactive flashcards.
 */
public class Main {

    private static final String TEST_FILES_DIR = "test-files";
    private static final String NO_DEFINITION = "Definition not found";

    private static final Scanner IN = new Scanner(System.in);

    // Materials the user has finished a quiz on this session (file path -> title).
    // These are what the "Previous test" option offers.
    private static final java.util.LinkedHashMap<String, String> COMPLETED = new java.util.LinkedHashMap<>();
    private static String currentPath = null; // file the current material was loaded from

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("        STUDY GUIDE GENERATOR (MVP)       ");
        System.out.println("==========================================");

        CourseMaterial material = null;

        // Optional: java studyguide.Main path/to/notes.txt
        if (args.length > 0) {
            material = loadFromFile(args[0], MaterialType.LECTURE_NOTES);
        }
        if (material == null) {
            material = chooseMaterial(false);
        }

        ConceptIdentifier identifier = new ConceptIdentifier();
        DefinitionMatcher matcher = new DefinitionMatcher();
        BulletPointGenerator bulletGenerator = new BulletPointGenerator(identifier);
        FlashCardGenerator flashCardGenerator = new FlashCardGenerator();

        boolean running = true;
        while (running) {
            System.out.println("\nCurrent material: " + material.getTitle()
                    + " (" + material.getType().getDisplayName() + ")");
            System.out.println("------------------------------------------");
            System.out.println("1. Show vocabulary list (terms + definitions)");
            System.out.println("2. Show key-concept bullet points");
            System.out.println("3. Start study session (review, then flashcard quiz)");
            System.out.println("4. Load different material");
            System.out.println("5. Change minimum term frequency (currently "
                    + identifier.getMinFrequency() + ")");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = IN.nextLine().trim();

            switch (choice) {
                case "1":
                    showVocabulary(material, identifier, matcher);
                    break;
                case "2":
                    showBullets(material, bulletGenerator);
                    break;
                case "3":
                    runStudySession(material, identifier, matcher, bulletGenerator, flashCardGenerator);
                    break;
                case "4":
                    CourseMaterial next = chooseMaterial(true);
                    if (next != null) {
                        material = next;
                    }
                    break;
                case "5":
                    changeFrequency(identifier);
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 0-5.");
            }
        }

        System.out.println("\nGood luck studying!");
    }

    // ---------------------------------------------------------------
    // Menu actions
    // ---------------------------------------------------------------

    private static void showVocabulary(CourseMaterial material, ConceptIdentifier identifier,
                                       DefinitionMatcher matcher) {
        String text = material.extractText();
        List<String> concepts = identifier.identify(material);

        if (concepts.isEmpty()) {
            System.out.println("\nNo vocabulary terms found. Try lowering the minimum term frequency (option 5).");
            return;
        }

        Map<String, String> vocab = matcher.matchDefinitions(concepts, text);

        // Only show terms that actually have a definition in the source material.
        int shown = 0;
        int skipped = 0;
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : vocab.entrySet()) {
            String definition = entry.getValue();
            if (definition == null || definition.isBlank() || definition.startsWith(NO_DEFINITION)) {
                skipped++;
                continue;
            }
            shown++;
            out.append("\n").append(capitalize(entry.getKey())).append("\n");
            out.append("   ").append(definition).append("\n");
        }

        if (shown == 0) {
            System.out.println("\nFound " + vocab.size() + " frequent terms, but none have a definition in this "
                    + "material.\nDefinitions need a line like \"Term: definition\" or \"Term is ...\".");
            return;
        }

        System.out.println("\n===== VOCABULARY (" + shown + " terms) =====");
        System.out.print(out);
        if (skipped > 0) {
            System.out.println("\n(" + skipped + " other frequent term" + (skipped == 1 ? "" : "s")
                    + " left out: no definition found in the material.)");
        }
    }

    private static void showBullets(CourseMaterial material, BulletPointGenerator generator) {
        System.out.println("\n===== KEY CONCEPTS =====");
        // "\u2022" (bullet) shows as "?" in some Windows consoles, so use a plain dash
        String raw = generator.generateFormattedBulletString(material).replace("\u2022", "-");
        System.out.println(cleanBullets(raw));
    }

    /**
     * Tidies bullet lines built from "Term: definition" source lines:
     *  - "- Methods: Methods: functions..." becomes "- Methods: functions..."
     *  - "- Computer: Memory: the hardware..." (a definition of a different term) is dropped.
     */
    private static String cleanBullets(String bullets) {
        StringBuilder out = new StringBuilder();
        for (String line : bullets.split("\\R")) {
            if (line.startsWith("- ")) {
                String body = line.substring(2);
                int firstColon = body.indexOf(": ");
                if (firstColon > 0) {
                    String label = body.substring(0, firstColon);
                    String rest = body.substring(firstColon + 2);
                    java.util.regex.Matcher m =
                            java.util.regex.Pattern.compile("^([A-Za-z][A-Za-z ]*): (.+)$").matcher(rest);
                    if (m.matches()) {
                        if (m.group(1).equalsIgnoreCase(label)) {
                            line = "- " + label + ": " + m.group(2);   // same term repeated: collapse
                        } else {
                            continue;                                  // someone else's definition: skip
                        }
                    }
                }
            }
            if (out.length() > 0) {
                out.append("\n");
            }
            out.append(line);
        }
        return out.toString();
    }

    /** Review (vocabulary + bullets) first, then the flashcard quiz. */
    private static void runStudySession(CourseMaterial material, ConceptIdentifier identifier,
                                        DefinitionMatcher matcher, BulletPointGenerator bulletGenerator,
                                        FlashCardGenerator flashCardGenerator) {
        List<Map<String, String>> cards = flashCardGenerator.generate(material);
        if (cards.isEmpty()) {
            System.out.println("\nNo flashcards could be made (no terms with definitions found).");
            System.out.println("Tip: definitions are found on lines like \"Term: definition\" or \"Term is ...\".");
            return;
        }

        System.out.print("\nReview the vocabulary and key concepts before the quiz? (Y/n): ");
        String answer = IN.nextLine().trim().toLowerCase();
        if (!answer.equals("n") && !answer.equals("no")) {
            showVocabulary(material, identifier, matcher);
            System.out.print("\nPress ENTER to see the key concepts...");
            IN.nextLine();
            showBullets(material, bulletGenerator);
            System.out.print("\nPress ENTER to start the quiz...");
            IN.nextLine();
        }

        flashCardGenerator.displayFlashCards(cards);

        // Quiz finished: remember it so it can be reopened under "Previous test"
        if (currentPath != null) {
            COMPLETED.put(currentPath, material.getTitle());
        }
    }

    private static void changeFrequency(ConceptIdentifier identifier) {
        System.out.print("New minimum frequency (1 or higher): ");
        try {
            int value = Integer.parseInt(IN.nextLine().trim());
            identifier.setMinFrequency(value);
            System.out.println("Minimum frequency set to " + value + ".");
        } catch (NumberFormatException e) {
            System.out.println("That's not a number. Keeping " + identifier.getMinFrequency() + ".");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Loading material
    // ---------------------------------------------------------------

    private static CourseMaterial chooseMaterial(boolean allowBack) {
        while (true) {
            System.out.println("\nWhat would you like to load?");
            System.out.println("1. Textbook (.txt file)");
            System.out.println("2. Class notes (.txt file)");
            System.out.println("3. Lecture slides (.txt file)");
            System.out.println("4. Previous test (a quiz you've already completed)");
            if (allowBack) {
                System.out.println("0. Back to menu");
            }
            System.out.print("Choose an option: ");

            String choice = IN.nextLine().trim();

            if (choice.equals("0") && allowBack) {
                return null;
            }

            if (choice.equals("4")) {
                CourseMaterial previous = loadPreviousTest();
                if (previous != null) {
                    return previous;
                }
                continue;
            }

            MaterialType type;
            switch (choice) {
                case "1": type = MaterialType.BOOK; break;
                case "2": type = MaterialType.LECTURE_NOTES; break;
                case "3": type = MaterialType.LECTURE_SLIDES; break;
                default:
                    System.out.println("Invalid option.");
                    continue;
            }

            String path = askForFile();
            CourseMaterial loaded = loadFromFile(path, type);
            if (loaded != null) {
                return loaded;
            }
            // loadFromFile already printed the error; loop and let them retry
        }
    }

    /**
     * "Previous test" = a material the user has already finished a quiz on.
     * Nothing completed yet -> say so. One completed -> open it automatically.
     */
    private static CourseMaterial loadPreviousTest() {
        if (COMPLETED.isEmpty()) {
            System.out.println("\nNo previous tests yet. Finish a study session first and it will show up here.");
            return null;
        }

        List<String> paths = new java.util.ArrayList<>(COMPLETED.keySet());
        String chosen;

        if (paths.size() == 1) {
            chosen = paths.get(0);
            System.out.println("\nOpening your previous test: " + COMPLETED.get(chosen));
        } else {
            System.out.println("\nPrevious tests:");
            for (int i = 0; i < paths.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + COMPLETED.get(paths.get(i)));
            }
            System.out.print("Enter a number: ");
            try {
                int n = Integer.parseInt(IN.nextLine().trim());
                if (n < 1 || n > paths.size()) {
                    System.out.println("Invalid choice.");
                    return null;
                }
                chosen = paths.get(n - 1);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                return null;
            }
        }

        return loadFromFile(chosen, MaterialType.PRIOR_TEST);
    }

    /** Lists .txt files in the test-files folder to pick from by number, or lets the user type a path. */
    private static String askForFile() {
        List<String> found = new java.util.ArrayList<>();
        java.io.File[] files = new java.io.File(TEST_FILES_DIR).listFiles();
        if (files != null) {
            java.util.Arrays.sort(files);
            for (java.io.File f : files) {
                if (f.isFile() && f.getName().toLowerCase().endsWith(".txt")) {
                    found.add(TEST_FILES_DIR + "/" + f.getName());
                }
            }
        }

        if (!found.isEmpty()) {
            System.out.println("\nFiles in " + TEST_FILES_DIR + "/:");
            for (int i = 0; i < found.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + found.get(i).substring(TEST_FILES_DIR.length() + 1));
            }
            System.out.print("Enter a number, or type the path to a .txt file: ");
        } else {
            System.out.print("Enter path to .txt file: ");
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

    private static CourseMaterial loadFromFile(String path, MaterialType type) {
        // PDF-SUPPORT (turned off for the demo): to go back to PDF-only, delete the .txt check
        // below and uncomment this one.
        // if (path == null || !path.toLowerCase().endsWith(".pdf")) {
        //     System.out.println("Only PDF files are supported (the path must end in .pdf).");
        //     return null;
        // }
        if (path == null || !path.toLowerCase().endsWith(".txt")) {
            System.out.println("Only .txt files are supported right now (the path must end in .txt).");
            return null;
        }
        if (!java.nio.file.Files.isRegularFile(java.nio.file.Path.of(path))) {
            System.out.println("File not found: " + path);
            return null;
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
                System.out.println("That file doesn't contain any readable text.");
                return null;
            }
            currentPath = path;
            System.out.println("Loaded \"" + title + "\" successfully.");
            System.out.println("Tip: definitions are found on lines like \"Term: definition\" or \"Term is ...\".");
            return material;

        } catch (IOException e) {
            System.out.println("Could not read that file.");
            return null;
        } catch (java.nio.file.InvalidPathException e) {
            System.out.println("That doesn't look like a valid file path.");
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static String fileNameWithoutExtension(String path) {
        String name = java.nio.file.Path.of(path).getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }

    private static String capitalize(String s) {
        return (s == null || s.isEmpty()) ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}