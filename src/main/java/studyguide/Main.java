package studyguide;

import book.Book;
import note.ClassNote;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
//        try {
//            MaterialParser parser = new MaterialParser();
//            ConceptIdentifier identifier = new ConceptIdentifier();
//            DefinitionMatcher matcher = new DefinitionMatcher();
//            FlashCardGenerator flashCardGenerator = new FlashCardGenerator();
//
//            //Load & Extract Textbook
//            Book book = parser.parseBook(".txt");
//            String text = book.getContent();
//
//            List<String> concepts = identifier.identify(text);
//            Map<String, String> flaschards = matcher.matchDefinitions(concepts, text);
//
//            String text = parser.parse("sampleNotes.txt");
//            List<String> concepts = identifier.identify(text);
//
//            System.out.println("Identified Vocabulary:");
//
//            for (String concept : concepts) {
//                System.out.println("- " + concept);
//            }
//
//            //Generate FlashCards
//            List<Map<String, String>> flashCards = flashCardGenerator.generate(material);
//
//            //Display Flashcard Session
//            flashCardGenerator.displayFlashCards(flashCards);
//
//        } catch (IOException e) {
//            System.out.println("Error reading study material: " + e.getMessage());
//        }
    }
}