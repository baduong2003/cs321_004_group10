package studyguide;

import java.util.*;

public class ConceptIdentifier {

    private static final int DEFAULT_MIN_FREQUENCY = 3;
    // Common English filler words and structural headers to ignore
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "about", "above", "after", "again", "against", "all", "also", "although",
            "always", "among", "another", "because", "been", "before", "being", "below",
            "between", "both", "cannot", "chapter", "could", "does", "doing", "down",
            "during", "each", "either", "enough", "every", "example", "first", "following",
            "from", "further", "given", "have", "having", "here", "however", "into",
            "itself", "just", "know", "like", "made", "make", "many", "might", "more",
            "most", "much", "must", "need", "never", "next", "notes", "often", "only",
            "other", "others", "otherwise", "over", "page", "pages", "part", "rather",
            "really", "right", "same", "section", "several", "shall", "should", "since",
            "slide", "slides", "some", "such", "take", "taken", "than", "that", "their",
            "them", "then", "there", "therefore", "these", "they", "thing", "things",
            "think", "this", "those", "though", "through", "thus", "together", "under",
            "until", "upon", "used", "using", "various", "very", "want", "well", "were",
            "what", "when", "where", "whether", "which", "while", "whom", "whose",
            "will", "with", "within", "without", "would", "your"
    ));
    private int minFrequency;

    public ConceptIdentifier() {
        this.minFrequency = DEFAULT_MIN_FREQUENCY;
    }

    public ConceptIdentifier(int minFrequency) {
        this.minFrequency = Math.max(1, minFrequency);
    }

    /**
     * Count repeated words and return important concepts sorted by frequency (highest first).
     *
     * @param text Content of the course material
     * @return List of important words that appear at least minFrequency (default 3) times
     */
    public List<String> identify(String text) {
        Map<String, Integer> frequencies = getConceptFrequencies(text);
        return extractSortedConcepts(frequencies, this.minFrequency);
    }

    /**
     * Overloaded helper: Identifies important concepts directly from a single CourseMaterial.
     */
    public List<String> identify(CourseMaterial material) {
        if (material == null || !material.isValid()) {
            return new ArrayList<>();
        }
        return identify(material.extractText());
    }

    /**
     * Overloaded helper: Identifies important concepts across multiple CourseMaterials.
     * Supports the "Prior Test" requirement by giving extra weight to terms that also appear in a previous test.
     */
    public List<String> identifyFromMaterials(List<CourseMaterial> materials) {
        if (materials == null || materials.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Integer> combinedFrequencies = new HashMap<>();

        for (CourseMaterial material : materials) {
            if (material == null || !material.isValid()) {
                continue;
            }
            // Prior tests receive a 2x weight boost to emphasize tested concepts
            int weight = (material.getType() == MaterialType.PRIOR_TEST) ? 2 : 1;
            Map<String, Integer> materialFreqs = getConceptFrequencies(material.extractText());

            for (Map.Entry<String, Integer> entry : materialFreqs.entrySet()) {
                combinedFrequencies.put(
                        entry.getKey(),
                        combinedFrequencies.getOrDefault(entry.getKey(), 0) + (entry.getValue() * weight)
                );
            }
        }

        return extractSortedConcepts(combinedFrequencies, this.minFrequency);
    }

    /**
     * Identifies repeated two-word key phrases (bigrams) that appear at least minFrequency times.
     */
    public List<String> identifyPhrases(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] words = cleanAndTokenize(text);
        Map<String, Integer> phraseFrequencies = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) {
            String first = words[i];
            String second = words[i + 1];

            if (isMeaningfulWord(first) && isMeaningfulWord(second)) {
                String phrase = first + " " + second;
                phraseFrequencies.put(phrase, phraseFrequencies.getOrDefault(phrase, 0) + 1);
            }
        }

        return extractSortedConcepts(phraseFrequencies, this.minFrequency);
    }

    /**
     * Returns the raw frequency map of meaningful words in the text.
     */
    public Map<String, Integer> getConceptFrequencies(String text) {
        Map<String, Integer> frequencies = new HashMap<>();
        if (text == null || text.trim().isEmpty()) {
            return frequencies;
        }

        String[] words = cleanAndTokenize(text);

        for (String word : words) {
            if (isMeaningfulWord(word)) {
                frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
            }
        }

        return frequencies;
    }



    // --- Private Helper Methods ---

    private String[] cleanAndTokenize(String text) {
        // Replace non-alphabetic characters (including newlines and punctuation) with a space
        // so words on adjacent lines do not merge together.
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z]+", " ")
                .trim()
                .split("\\s+");
    }

    private boolean isMeaningfulWord(String word) {
        return word != null
                && word.length() > 3
                && !STOP_WORDS.contains(word);
    }

    private List<String> extractSortedConcepts(Map<String, Integer> frequencies, int threshold) {
        List<String> concepts = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() >= threshold) {
                concepts.add(entry.getKey());
            }
        }

        // Sort words by frequency (highest first), then alphabetically on ties
        concepts.sort((wordA, wordB) -> {
            int countA = frequencies.get(wordA);
            int countB = frequencies.get(wordB);
            if (countA != countB) {
                return Integer.compare(countB, countA);
            }
            return wordA.compareTo(wordB);
        });

        return concepts;
    }

    public int getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(int minFrequency) {
        if (minFrequency <= 0) {
            throw new IllegalArgumentException("Minimum frequency must be at least 1.");
        }
        this.minFrequency = minFrequency;
    }
}