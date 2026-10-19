package studyguide;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MaterialParser {

    public String parse(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String content = Files.readString(path);

        return content;
    }
}
