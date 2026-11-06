package java.studyguide;

import org.junit.jupiter.api.Test;
import studyguide.MaterialParser;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
