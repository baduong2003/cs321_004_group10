package studyguide;

import java.util.*;

public class ConceptIdentifier {

    /**
     * Count repeated words
     * @param text Content of the book
     * @return List of words that appear more than 3 times
     */
    public List<String> identify(String text) {

        String[] words = text.toLowerCase()
                .replaceAll("[^a-zA-Z ]", "")
                .split("\\s+");

        Map<String, Integer> frequencies = new HashMap<>();

        for (String word : words) {
            if (word.length() > 3) {
                frequencies.put(
                        word,
                        frequencies.getOrDefault(word, 0) + 1
                );
            }
        }

        List<String> concepts = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() >= 3) {
                concepts.add(entry.getKey());
            }
        }

        return concepts;
    }
}