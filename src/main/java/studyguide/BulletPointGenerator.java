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
            String bestSentence = findBestSentenceForConcept(sentences, cleanConcept, usedSentences, concepts);

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

    /**
     * Picks the most useful sentence about a concept instead of simply the first one that mentions it.
     * See scoreSentence for what makes a sentence a good study bullet.
     */
    private String findBestSentenceForConcept(String[] sentences, String concept, Set<String> usedSentences,
                                              List<String> allConcepts) {
        String lowerConcept = concept.toLowerCase();
        String best = null;
        int bestScore = Integer.MIN_VALUE;

        for (String rawSentence : sentences) {
            String sentence = rawSentence.trim();
            // Skip structural headers like "Chapter 1: Intro" or very short fragments
            if (sentence.length() < 15 || isHeaderLine(sentence)) {
                continue;
            }
            if (usedSentences.contains(sentence) || !containsWholeWord(sentence.toLowerCase(), lowerConcept)) {
                continue;
            }
            int score = scoreSentence(sentence, lowerConcept, allConcepts);
            if (score > bestScore) {   // strictly greater: on a tie the earlier sentence wins
                bestScore = score;
                best = sentence;
            }
        }
        return best;
    }

    // Phrases that signal a sentence explains something ("X lets/prevents/allows ...").
    private static final String[] EXPLAINING_CUES = {
            "because", "allows", "lets ", "prevents", "ensures", "so that", "used to", "which ", "when ",
            "causes", "makes ", "requires", "instead", "cannot", "must ", "slow", "fast", "cheap", "by "
    };

    /**
     * Higher = better study bullet.
     *  + explains something (because / allows / prevents ...)
     *  + connects this concept to other key concepts
     *  - depends on the previous sentence ("Its main job...", "To do this well...", "...all of this...")
     *  - looks like an exam question or instruction ("Define deadlock...", "3. Compare...")
     *  - too long or too short
     *  - is only the plain definition ("X is ..."): still allowed, but a more informative fact wins
     */
    private int scoreSentence(String sentence, String lowerConcept, List<String> allConcepts) {
        String lower = sentence.toLowerCase();
        int score = 0;

        if (lower.matches("^(it|its|this|that|these|those|they|their|them|however|therefore|thus|also|but|and|so|"
                + "moreover|furthermore|for this reason|to do this|in addition|modern|early)\\b.*")) {
            score -= 8;
        }
        if (lower.matches(".*\\b(this|these|those)\\b.*")) {
            score -= 4;
        }
        if (sentence.endsWith("?") || sentence.matches("^\\d+[.)].*")
                || lower.matches("^(define|explain|compare|describe|state|name|give|list|what|why|how|which)\\b.*")) {
            score -= 10;
        }

        // Study advice / exam chatter is not a fact about the concept
        if (lower.matches(".*\\b(midterm|exam|quiz|homework|professor|instructor|remember|show up|on the test)\\b.*")) {
            score -= 8;
        }

        // The concept is the subject (near the start) rather than a passing mention
        int position = lower.indexOf(lowerConcept);
        if (position >= 0 && position <= 40) {
            score += 3;
        }

        for (String cue : EXPLAINING_CUES) {
            if (lower.contains(cue)) {
                score += 4;
                break;
            }
        }

        int others = 0;
        for (String other : allConcepts) {
            if (other != null && !other.isBlank() && !other.trim().equalsIgnoreCase(lowerConcept)
                    && containsWholeWord(lower, other.trim().toLowerCase())) {
                others++;
            }
        }
        score += Math.min(others, 2) * 2;

        if (sentence.length() > 220) {
            score -= 4;
        }
        if (sentence.length() < 30) {
            score -= 3;
        }
        if (isDefinitionOf(sentence, lowerConcept)) {
            score -= 2;
        }
        return score;
    }

    private boolean isDefinitionOf(String sentence, String lowerConcept) {
        return sentence.toLowerCase().matches("^(?:(?:a|an|the)\\s+)?" + java.util.regex.Pattern.quote(lowerConcept)
                + "(?:s|es)?\\s*(?:is|are|means|refers to|:|-).*");
    }

    private boolean isHeaderLine(String sentence) {
        String lower = sentence.toLowerCase();
        return (lower.startsWith("chapter ") || lower.startsWith("slide ") || lower.startsWith("notes:"))
                && !sentence.endsWith(".");
    }

    private boolean containsWholeWord(String text, String word) {
        // Checks that the concept appears as a standalone word/phrase
        String pattern = ".*\\b" + java.util.regex.Pattern.quote(word) + "(?:s|es)?\\b.*";
        return text.matches(pattern);
    }

    private String formatBulletPoint(String concept, String sentence) {
        // Capitalize the first letter of the concept for a clean label
        String capitalizedConcept = concept.substring(0, 1).toUpperCase() + concept.substring(1);
        // Don't repeat the term: "Process: A process is a program ..." becomes "Process: a program ..."
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "^(?:(?:a|an|the)\\s+)?" + java.util.regex.Pattern.quote(concept) + "(?:s|es)?\\s*(?:is|are|means|refers to|:|-)\\s+(.{15,})$",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(sentence);
        if (m.find()) {
            sentence = m.group(1);
        }
        return "• " + capitalizedConcept + ": " + sentence;
    }

    public ConceptIdentifier getConceptIdentifier() {
        return conceptIdentifier;
    }
}