package studyguide;

import java.util.*;

/**
 * Combines several course materials (textbook, class notes, lecture slides, previous tests)
 * into one study guide: one list of important terms, each with a definition found in the
 * materials themselves and a note of which material it came from.
 */
public class StudyGuideBuilder {

    /** One vocabulary entry. */
    public static class Entry {
        public final String term;
        public final String definition;
        public final String source;

        Entry(String term, String definition, String source) {
            this.term = term;
            this.definition = definition;
            this.source = source;
        }
    }

    private static final String NO_DEFINITION = "Definition not found";
    private static final int CHUNK = 100;

    private final List<CourseMaterial> materials = new ArrayList<>();
    private final ConceptIdentifier identifier;
    private final DefinitionMatcher matcher;

    public StudyGuideBuilder(ConceptIdentifier identifier, DefinitionMatcher matcher) {
        this.identifier = identifier;
        this.matcher = matcher;
    }

    public void add(CourseMaterial material) {
        if (material != null && material.isValid()) {
            materials.add(material);
        }
    }

    public CourseMaterial remove(int index) {
        return materials.remove(index);
    }

    public void clear() {
        materials.clear();
    }

    public List<CourseMaterial> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    public boolean isEmpty() {
        return materials.isEmpty();
    }

    /** All loaded text joined together (used for the key-concept bullets). */
    public String combinedText() {
        StringBuilder sb = new StringBuilder();
        for (CourseMaterial m : materials) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(m.extractText());
        }
        return sb.toString();
    }

    /**
     * Builds up to maxTerms vocabulary entries, most frequent terms first.
     * Terms are counted across ALL materials (previous tests count double, see ConceptIdentifier).
     * A definition is taken from the first material, in priority order, that actually defines the term.
     */
    public List<Entry> build(int maxTerms) {
        List<Entry> entries = new ArrayList<>();
        if (materials.isEmpty() || maxTerms <= 0) {
            return entries;
        }

        // Two-word terms ("race condition") come first, then single words. A word that only has the
        // same definition as a phrase already listed is skipped below, so "condition" won't repeat it.
        List<String> concepts = new ArrayList<>(identifier.identifyPhrases(combinedText()));
        concepts.addAll(identifier.identifyFromMaterials(materials));
        List<CourseMaterial> ordered = byPriority();

        // Cache each material's text once; extractText() can be expensive for big books.
        Map<CourseMaterial, String> texts = new LinkedHashMap<>();
        for (CourseMaterial m : ordered) {
            texts.put(m, m.extractText());
        }

        Set<String> usedDefinitions = new HashSet<>();

        // Work through the frequent terms in chunks so a huge textbook doesn't mean thousands of lookups.
        for (int start = 0; start < concepts.size() && entries.size() < maxTerms; start += CHUNK) {
            List<String> chunk = concepts.subList(start, Math.min(start + CHUNK, concepts.size()));

            Map<String, String> foundDefinition = new HashMap<>();
            Map<String, String> foundSource = new HashMap<>();
            List<String> remaining = new ArrayList<>(chunk);

            for (Map.Entry<CourseMaterial, String> mat : texts.entrySet()) {
                if (remaining.isEmpty()) {
                    break;
                }
                Map<String, String> matches = matcher.matchDefinitions(remaining, mat.getValue());
                for (Map.Entry<String, String> m : matches.entrySet()) {
                    String def = m.getValue();
                    if (def != null && !def.isBlank() && !def.startsWith(NO_DEFINITION)) {
                        foundDefinition.put(m.getKey(), def);
                        foundSource.put(m.getKey(), mat.getKey().getTitle());
                    }
                }
                remaining.removeAll(foundDefinition.keySet());
            }

            for (String concept : chunk) {
                if (entries.size() >= maxTerms) {
                    break;
                }
                String def = foundDefinition.get(concept);
                // Skip terms with no definition, and plural/singular twins that share a definition
                if (def != null && usedDefinitions.add(def.toLowerCase())) {
                    entries.add(new Entry(concept, def, foundSource.get(concept)));
                }
            }
        }

        return entries;
    }

    /** Textbook definitions first, then slides, then notes, then previous tests. */
    private List<CourseMaterial> byPriority() {
        List<CourseMaterial> ordered = new ArrayList<>(materials);
        ordered.sort(Comparator.comparingInt(m -> rank(m.getType())));
        return ordered;
    }

    private static int rank(MaterialType type) {
        switch (type) {
            case BOOK: return 0;
            case LECTURE_SLIDES: return 1;
            case LECTURE_NOTES: return 2;
            default: return 3;
        }
    }
}