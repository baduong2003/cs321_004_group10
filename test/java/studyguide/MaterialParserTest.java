package studyguide;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MaterialParserTest {

    @Test
    void parseReadsTextFile() throws Exception {
        Path file = Path.of("test-notes.txt");
        String expected = "Operating systems manage resources.";

        Files.writeString(file, expected);

        MaterialParser parser = new MaterialParser();
        String actual = parser.parse(file.toString());

        assertEquals(expected, actual);

        Files.delete(file);
    }

    @Test
    void parseReadsPdfFile() throws Exception {
        Path file = Path.of("test-files/sample.pdf");

        MaterialParser parser = new MaterialParser();
        String actual = parser.parse(file.toString());

        assertTrue(actual.contains("Introduction to Computer Science"));
        assertTrue(actual.contains("Algorithms"));
        assertTrue(actual.contains("Data Structures"));
        assertTrue(actual.contains("Operating Systems"));
    }

    @Test
    void parsePdfAndIdentifyConcepts() throws Exception {
        MaterialParser parser = new MaterialParser();
        ConceptIdentifier identifier = new ConceptIdentifier();

        String text = parser.parse("test-files/sample.pdf");
        List<String> concepts = identifier.identify(text);

        assertTrue(concepts.contains("algorithm"));
        assertTrue(concepts.contains("data"));
        assertTrue(concepts.contains("process"));
    }
}