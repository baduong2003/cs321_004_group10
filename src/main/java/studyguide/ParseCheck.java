package studyguide;

/**
 * Quick check that PDF reading works. Not part of the app: run it on any PDFs, then delete it.
 *   java -cp "out;lib/*" studyguide.ParseCheck file1.pdf file2.pdf
 */
public class ParseCheck {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java -cp \"out;lib/*\" studyguide.ParseCheck <file.pdf> [more.pdf ...]");
            return;
        }
        MaterialParser parser = new MaterialParser();
        for (String file : args) {
            System.out.println("\n=== " + file + " ===");
            try {
                String text = parser.parse(file);
                if (text.isBlank()) {
                    System.out.println("NO TEXT FOUND (scanned/image-only PDF?)");
                    continue;
                }
                int words = text.split("\\s+").length;
                int lines = text.split("\n").length;
                System.out.println("OK: " + words + " words, " + lines + " lines");
                System.out.println("--- first 500 characters ---");
                System.out.println(text.substring(0, Math.min(500, text.length())));
            } catch (Exception e) {
                System.out.println("FAILED: " + e.getMessage());
            }
        }
    }
}