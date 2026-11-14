package studyguide;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class MaterialParser {

    public String parse(String filePath) throws IOException {
        Path path = Path.of(filePath);

        if (filePath.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(path.toFile())) {
                PDFTextStripper stripper = new PDFTextStripper();
                return unwrapLines(stripper.getText(document));
            }
        }

        return Files.readString(path);
    }

    /**
     * PDFs store text as visual lines, so a single sentence is often split across
     * several lines. This rejoins those wrapped lines so definitions and sentences
     * stay whole for DefinitionMatcher and BulletPointGenerator.
     *
     * A line is joined to the next one when it does not end a sentence
     * (. ! ? : ;) and the next line starts with a lowercase letter, or when it
     * ends with a comma. Words hyphenated across a line break are rejoined.
     */
    static String unwrapLines(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String current = lines[i].strip();
            out.append(current);

            if (i + 1 >= lines.length) {
                break;
            }

            String next = lines[i + 1].strip();
            boolean join = false;
            boolean dehyphenate = false;

            if (!current.isEmpty() && !next.isEmpty()) {
                char last = current.charAt(current.length() - 1);
                char first = next.charAt(0);

                if (last == ',') {
                    join = true;
                } else if (last == '-' && current.length() > 1
                        && Character.isLetter(current.charAt(current.length() - 2))
                        && Character.isLowerCase(first)) {
                    join = true;
                    dehyphenate = true;
                } else if (".!?:;".indexOf(last) < 0 && Character.isLowerCase(first)) {
                    join = true;
                }
            }

            if (dehyphenate) {
                out.setLength(out.length() - 1); // drop the trailing hyphen, no space
            } else if (join) {
                out.append(' ');
            } else {
                out.append('\n');
            }
        }

        return out.toString().trim();
    }
}