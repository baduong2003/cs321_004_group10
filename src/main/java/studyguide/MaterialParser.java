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
                return stripper.getText(document);
            }
        }

        return Files.readString(path);
    }
}