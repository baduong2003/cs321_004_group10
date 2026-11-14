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

    // Built-in sample so the demo works even if no file is available.
    // Each key term appears 3+ times (ConceptIdentifier's default threshold)
    // and has a "Term: definition" line (what DefinitionMatcher looks for).
    private static final String SAMPLE_NOTES =
            "OOP Study Guide\n"
            + "Object: an instance of a class that holds its own data and behavior.\n"
            + "Methods: functions defined inside a class that describe what its objects can do.\n"
            + "Classes: blueprints that define the fields and methods an object will have.\n"
            + "Every object is created from a class, and each object keeps its own state.\n"
            + "Objects interact by calling each other's methods.\n"
            + "\n"
            + "Encapsulation: bundling data and the methods that operate on it inside one class.\n"
            + "Encapsulation protects an object's internal state from outside interference.\n"
            + "Good encapsulation means other classes use public methods instead of touching fields directly.\n"
            + "\n"
            + "Inheritance: a mechanism where a subclass acquires the fields and methods of a superclass.\n"
            + "Inheritance promotes code reuse across related classes.\n"
            + "A subclass can override methods it gets through inheritance.\n"
            + "\n"
            + "Polymorphism: the ability of one interface to be used with many different underlying types.\n"
            + "Polymorphism lets a single method call behave differently depending on the object.\n"
            + "Overriding is a common way to achieve polymorphism in Java.\n"
            + "\n"
            + "Abstraction: hiding complex implementation details and exposing only what is necessary.\n"
            + "Abstraction is often achieved with abstract classes and interfaces.\n"
            + "Abstraction keeps programmers focused on what an object does, not how it does it.\n";

    private static final Scanner IN = new Scanner(System.in);

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
            material = chooseMaterial();
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
            System.out.println("3. Start flashcard quiz");
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
                    runFlashcards(material, flashCardGenerator);
                    break;
                case "4":
                    CourseMaterial next = chooseMaterial();
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

        System.out.println("\n===== VOCABULARY (" + vocab.size() + " terms) =====");
        for (Map.Entry<String, String> entry : vocab.entrySet()) {
            System.out.println("\n" + capitalize(entry.getKey()));
            System.out.println("   " + entry.getValue());
        }
    }

    private static void showBullets(CourseMaterial material, BulletPointGenerator generator) {
        System.out.println("\n===== KEY CONCEPTS =====");
        System.out.println(generator.generateFormattedBulletString(material));
    }

    private static void runFlashcards(CourseMaterial material, FlashCardGenerator generator) {
        List<Map<String, String>> cards = generator.generate(material);
        if (cards.isEmpty()) {
            System.out.println("\nNo flashcards could be made (no terms with definitions found).");
            return;
        }
        generator.displayFlashCards(cards);
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

    private static CourseMaterial chooseMaterial() {
        while (true) {
            System.out.println("\nWhat would you like to load?");
            System.out.println("1. Built-in sample notes (OOP)");
            System.out.println("2. Textbook (.txt file)");
            System.out.println("3. Class notes (.txt file)");
            System.out.println("4. Lecture slides (.txt file)");
            System.out.println("5. Previous test (.txt file)");
            System.out.print("Choose an option: ");

            String choice = IN.nextLine().trim();

            if (choice.equals("1")) {
                return new CourseMaterial(new ClassNote("OOP Sample Notes", SAMPLE_NOTES));
            }

            MaterialType type;
            switch (choice) {
                case "2": type = MaterialType.BOOK; break;
                case "3": type = MaterialType.LECTURE_NOTES; break;
                case "4": type = MaterialType.LECTURE_SLIDES; break;
                case "5": type = MaterialType.PRIOR_TEST; break;
                default:
                    System.out.println("Invalid option.");
                    continue;
            }

            System.out.print("Enter path to .txt file: ");
            String path = IN.nextLine().trim().replaceAll("^\"|\"$", ""); // strips quotes from "Copy as path"
            CourseMaterial loaded = loadFromFile(path, type);
            if (loaded != null) {
                return loaded;
            }
            // loadFromFile already printed the error; loop and let them retry
        }
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
            System.out.println("Loaded \"" + title + "\" successfully.");
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