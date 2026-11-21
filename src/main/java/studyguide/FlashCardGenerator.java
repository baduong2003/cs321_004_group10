package studyguide;

import java.util.*;

public class FlashCardGenerator {
    private final ConceptIdentifier conceptIdentifier;
    private final DefinitionMatcher definitionMatcher;

    public FlashCardGenerator() {
        this.conceptIdentifier = new ConceptIdentifier();
        this.definitionMatcher = new DefinitionMatcher();
    }

    /**
     * Generating Flashcards Directly from CourseMaterial
     */
    public List<Map<String, String>> generate(CourseMaterial material)  {
        List<Map<String, String>> flashCards = new ArrayList<>();

        if(material == null || !material.isValid())    {
            return flashCards;
        }

        String sourceText = material.extractText();

        if(sourceText == null || sourceText.isBlank())  {
            return flashCards;
        }

        //Important Concepts
        List<String> concepts = conceptIdentifier.identify(material);

        //Match Concepts
        Map<String, String> definitions = definitionMatcher.matchDefinitions(concepts, sourceText);

        //FlashCard Generation
        return generateFromDefinitions(definitions, material.getTitle());
    }

    /**
     * Generates FlashCards from Existing Definition Map
     */
    public List<Map<String, String>> generateFromDefinitions(Map<String, String> definitions, String sourceTitle)   {
        List<Map<String, String>> flashCards = new ArrayList<>();

        if(definitions == null || definitions.isEmpty())    {
            return flashCards;
        }

        Set<String> seen = new HashSet<>();

        for(Map.Entry<String, String> entry : definitions.entrySet())  {
            String concept = entry.getKey();
            String definition = entry.getValue();

            if(concept == null || concept.isBlank() || definition == null || definition.isBlank() || definition.equalsIgnoreCase("Definition not found in source material.")) {
                continue;
            }

            //Ignore Duplications
            if(!seen.add(concept.trim().toLowerCase())) {
                continue;
            }

            Map<String, String> card = new LinkedHashMap<>();

            card.put("question", "What is " + concept + "?");
            card.put("answer", definition.trim());
            card.put("difficulty", determineDifficulty(definition));
            card.put("topic", concept);
            card.put("source", sourceTitle);

            flashCards.add(card);
        }

        return flashCards;
    }

    private String determineDifficulty(String definition)    {
        if(definition == null || definition.isBlank())  {
            return "Non-Existent";
        }

        int wordCount = definition.trim().split("\\s+").length;

        if(wordCount <= 10)  {
            return "Easy";
        } else if(wordCount <= 25)  {
            return "Medium";
        } else  {
            return "Hard";
        }
    }

    /**
     * Generated FlashCard Display
     */
    // Only used by the no-argument version below. It is a field, not a local variable, and System.in must stay
    // open for the rest of the program, so it is never closed.
    private static final Scanner KEYBOARD = new Scanner(System.in);

    public void displayFlashCards(List<Map<String, String>> flashCards) {
        displayFlashCards(flashCards, KEYBOARD);
    }

    /**
     * Same quiz, but reads answers from the caller's Scanner, so the whole program shares ONE Scanner on the
     * keyboard. The caller owns the Scanner and closes it, if ever, when the program ends.
     */
    public void displayFlashCards(List<Map<String, String>> flashCards, Scanner scanner) {
        if(flashCards == null || flashCards.isEmpty())  {
            System.out.println("No flashcards available.");
            return;
        }

        flashCards.removeIf(card -> card == null || card.get("question") == null || card.get("answer") == null || card.get("answer").isBlank());

        List<Map<String, String>> correctPile = new ArrayList<>();
        List<Map<String, String>> incorrectPile = new ArrayList<>();

        System.out.println("\n===== GENERATED FLASHCARDS INCOMING =====");

        int count = 1;
        int closenessTotal = 0;
        
        for(Map<String, String> card : flashCards)  {
            System.out.println("\n===== LOCK IN ======");
            System.out.println("\nFlashcard #" + count++);
            System.out.println("Difficulty: " + card.get("difficulty"));

            System.out.println("\nQUESTION: ");
            System.out.println(card.get("question"));

            System.out.println("\nType in your Answer: ");
            String studentAnswer = scanner.nextLine();

            String correctAnswer = card.get("answer");

            System.out.println("\nCORRECT ANSWER: ");
            System.out.println(correctAnswer);

            AnswerGrader.Result grade = AnswerGrader.grade(studentAnswer, correctAnswer);
            closenessTotal += grade.getPercent();

            System.out.println("\nYour answer covered " + grade.getPercent() + "% of the key words.");

            if(grade.isCredited())   {
                System.out.println("\nWOOHOO YOU GOT IT RIGHT");
                correctPile.add(card);
            } else  {
                if(grade.isClose()) {
                    System.out.println("\nSO CLOSE! You need " + (int) Math.round(AnswerGrader.CREDIT_THRESHOLD * 100)
                            + "% for credit.");
                } else  {
                    System.out.println("\nWOMP WOMP, c'mon try harder");
                }
                if(!grade.getMissedKeywords().isEmpty())    {
                    System.out.println("Missing: " + String.join(", ", grade.getMissedKeywords()));
                }
                incorrectPile.add(card);
            }

            System.out.println("\nPress ENTER for the next flashcard...");
            scanner.nextLine();
        }

        int totalCards = flashCards.size();
        int correctCount = correctPile.size();
        int incorrectCount = incorrectPile.size();

        double correctPercentage = (double) correctCount / totalCards * 100;
        double incorrectPercentage = (double) incorrectCount / totalCards * 100;

        System.out.println("\n===== STUDY SESSION COMPLETE =====");
        System.out.println("Total Cards Attempted: " + totalCards);

        System.out.println("\nCORRECT PILE: " + correctCount);
        System.out.println("\nINCORRECT PILE: " + incorrectCount);

        if(correctPercentage == 100)    {
            System.out.println("\nYOU ARE READY");
            System.out.printf("Correct Percentage: %.2f%%%n", correctPercentage);
        } else if(correctPercentage > incorrectPercentage)  {
            System.out.println("\nYOU GOT MORE RIGHT THAN WRONG");
            System.out.printf("Correct Percentage: %.2f%%%n", correctPercentage);
        } else {
            System.out.println("\nYou have a lot of work left to do...");
            System.out.printf("Correct Percentage: %.2f%%%n", correctPercentage);
        }

        System.out.printf("Incorrect Percentage: %.2f%%%n", incorrectPercentage);
        System.out.printf("Average closeness to the correct answers: %.0f%%%n", (double) closenessTotal / totalCards);

        System.out.println("\n===== STUDY THESE MORE BEFORE YOU TRY AGAIN =====");
        
        for(Map<String, String> card : incorrectPile)   {
            System.out.println("\nQuestion: " + card.get("question"));
            System.out.println("\nAnswer: " + card.get("answer"));
        }
        
        System.out.println("\nTotal Flashcards: " + flashCards.size());

    }
}