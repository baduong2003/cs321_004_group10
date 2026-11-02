package studyguide;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            MaterialParser parser = new MaterialParser();
            ConceptIdentifier identifier = new ConceptIdentifier();

            String text = parser.parse("sampleNotes.txt");
            List<String> concepts = identifier.identify(text);

            System.out.println("Identified Vocabulary:");

            for (String concept : concepts) {
                System.out.println("- " + concept);
            }

        } catch (IOException e) {
            System.out.println("Error reading study material: " + e.getMessage());
        }
    }
}