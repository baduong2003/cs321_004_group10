
package studyguide;

import java.io.IOException;
import java.util.List;

public class MaterialParserTest {

    public static void main(String[] args) {
        MaterialParser parser = new MaterialParser();

        // Test 1: Read PDF file
        try {
            String text = parser.parse("test-files/sample.pdf");

            System.out.println("PDF Extraction Test:");
            System.out.println(text);

            if (text.contains("Introduction to Computer Science")
                    && text.contains("Algorithms")
                    && text.contains("Data Structures")
                    && text.contains("Operating Systems")) {
                System.out.println("PASS: PDF text extraction");
            } else {
                System.out.println("FAIL: Expected text was not found");
            }

        } catch (IOException e) {
            System.out.println("FAIL: PDF extraction - " + e.getMessage());
        }

        // Test 2: Reject non-PDF files
        try {
            parser.parse("test-notes.txt");
            System.out.println("FAIL: Non-PDF file was accepted");

        } catch (IOException e) {
            System.out.println("PASS: Non-PDF file rejected");
        }

        // Test 3: Extract PDF text and identify concepts
        try {
            String text = parser.parse("test-files/sample.pdf");
            ConceptIdentifier identifier = new ConceptIdentifier();
            List<String> concepts = identifier.identify(text);

            if (concepts.contains("algorithm")
                    && concepts.contains("data")
                    && concepts.contains("process")) {
                System.out.println("PASS: Concept identification");
            } else {
                System.out.println("FAIL: Expected concepts were not identified");
                System.out.println("Found concepts: " + concepts);
            }

        } catch (IOException e) {
            System.out.println("FAIL: Concept identification test - " + e.getMessage());
        }
    }
}
