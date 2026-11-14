package studyguide;

import java.util.*;

public class BulletPointGenerator {
    private final ConceptIdentifier conceptIdentifier;

    public BulletPointGenerator() {
        this.conceptIdentifier = new ConceptIdentifier();
    }

    public BulletPointGenerator(ConceptIdentifier conceptIdentifier) {
        if (conceptIdentifier == null) {
            throw new IllegalArgumentException("ConceptIdentifier cannot be null");
        }
        this.conceptIdentifier = conceptIdentifier;
    }

    /**
     * Generates bullet points for the given text based on identified concepts.
     * @param text The input text from which to generate bullet points.
     * @return A list of bullet points, each corresponding to a concept and its context.
     */
    public List<String> generateBulletPoints(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> concepts = conceptIdentifier.identify(text);
        return generateBulletPoints(text, concepts);
    }

    /**
     * Generates bullet points for the given CourseMaterial based on identified concepts.
     * @param material The CourseMaterial from which to generate bullet points.
     * @return A list of bullet points, each corresponding to a concept and its context.
     */
    public List<String> generateBulletPoints(CourseMaterial material) {
        if (material == null || !material.isValid()) {
            return new ArrayList<>();
        }
        String extractedText = material.extractText();
        List<String> concepts = conceptIdentifier.identify(extractedText);
        return generateBulletPoints(extractedText, concepts);
    }

    /**
     * Generates bullet points for the given CourseMaterial based on identified concepts.
     * @param text material The CourseMaterial from which to generate bullet points.
     * @return A list of bullet points, each corresponding to a concept and its context.
     */
    public List<String> generateBulletPoints(String text, List<String> concepts) {
        List<String> bulletPoints = new ArrayList<>();
        if (text == null || text.trim().isEmpty() || concepts == null || concepts.isEmpty()) {
            return bulletPoints;
        }

        String[] sentences = splitIntoSentences(text);
        Set<String> usedSentences = new HashSet<>();

        for (String concept : concepts) {
            if (concept == null || concept.trim().isEmpty()) {
                continue;
            }
            String cleanConcept = concept.trim();
            String bestSentence = findBestSentenceForConcept(sentences, cleanConcept, usedSentences);

            if (bestSentence != null) {
                usedSentences.add(bestSentence);
                bulletPoints.add(formatBulletPoint(cleanConcept, bestSentence));
            }
        }

        return bulletPoints;
    }

    /**
     * Generates a formatted string of bullet points for the given CourseMaterial.
     * @param material The CourseMaterial from which to generate bullet points.
     * @return A formatted string of bullet points, or a message if no bullet points are found.
     */
    public String generateFormattedBulletString(CourseMaterial material) {
        List<String> bullets = generateBulletPoints(material);
        if (bullets.isEmpty()) {
            return "No key concepts or bullet points found.";
        }
        return String.join("\n", bullets);
    }



    // --- Private Helper Methods ---

    private String[] splitIntoSentences(String text) {
        // Split on sentence-ending punctuation (. ! ?) or newline boundaries
        return text.trim().split("(?<=[.!?])\\s+|\\r?\\n+");
    }

    private String findBestSentenceForConcept(String[] sentences, String concept, Set<String> usedSentences) {
        String lowerConcept = concept.toLowerCase();

        for (String rawSentence : sentences) {
            String sentence = rawSentence.trim();
            // Skip structural headers like "Chapter 1: Intro" or very short fragments
            if (sentence.length() < 15 || isHeaderLine(sentence)) {
                continue;
            }

            if (!usedSentences.contains(sentence) && containsWholeWord(sentence.toLowerCase(), lowerConcept)) {
                return sentence;
            }
        }
        return null;
    }

    private boolean isHeaderLine(String sentence) {
        String lower = sentence.toLowerCase();
        return (lower.startsWith("chapter ") || lower.startsWith("slide ") || lower.startsWith("notes:"))
                && !sentence.endsWith(".");
    }

    private boolean containsWholeWord(String text, String word) {
        // Checks that the concept appears as a standalone word/phrase
        String pattern = ".*\\b" + java.util.regex.Pattern.quote(word) + "\\b.*";
        return text.matches(pattern);
    }

    private String formatBulletPoint(String concept, String sentence) {
        // Capitalize the first letter of the concept for a clean label
        String capitalizedConcept = concept.substring(0, 1).toUpperCase() + concept.substring(1);
        return "• " + capitalizedConcept + ": " + sentence;
    }

    public ConceptIdentifier getConceptIdentifier() {
        return conceptIdentifier;
    }
}
