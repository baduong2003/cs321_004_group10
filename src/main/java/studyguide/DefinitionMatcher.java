package studyguide;

import java.util.*;
import java.util.regex.*;

/**
 * Finds a definition for each frequent term by searching the source text.
 *
 * Tier 1 (glossary / line start):  "Term: definition", "Term - definition", "Term - definition",
 *        "Term is/are/means/refers to ..." at the start of a line.
 * Tier 2 (sentence level):         "A term is ...", "Deadlock: ...", i.e. the term as the subject of a
 *        defining sentence, anywhere in a paragraph (optional article in front, nothing else).
 * Tier 3 (reverse form):           "A program in execution is called a process."
 *
 * If nothing matches, the term gets NOT_FOUND. A sentence that merely MENTIONS the term is never
 * used as its definition, and definitions are only ever taken from the text.
 */
public class DefinitionMatcher {

    private static final String NOT_FOUND = "Definition not found in source material.";

    private static final String LINKING_VERBS =
            "(?:is\\s+defined\\s+as|can\\s+be\\s+defined\\s+as|is\\s+known\\s+as|refers?\\s+to|means|describes|is|are)";

    // A dash only separates a term from its definition if it has a space before it (or is an en/em dash),
    // so "Object-oriented programming ..." is not read as "Object - oriented programming ...".
    private static final String SEPARATOR = "(?::|\\s*[\\u2013\\u2014]|\\s-)";

    // "Memory management is just as important as ..." is a comment, not a definition.
    private static final Set<String> NON_DEFINITION_STARTS = new HashSet<>(Arrays.asList(
            "just", "also", "very", "not", "no", "often", "usually", "always", "never", "still", "really",
            "more", "less", "too", "quite", "rather", "only", "even", "now", "then", "simply", "generally",
            "typically", "equally", "especially", "particularly", "mostly", "mainly", "largely", "currently",
            "important", "necessary", "possible", "impossible", "useful", "difficult", "easy", "likely"));

    // "is created from ...", "are stored in ..." describe what happens to something, not what it is.
    private static final Set<String> PASSIVE_FOLLOWERS = new HashSet<>(Arrays.asList(
            "by", "from", "to", "in", "on", "with", "for", "as", "at", "into", "through", "during", "between", "within"));

    public Map<String, String> matchDefinitions(List<String> concepts, String text) {

        Map<String, String> vocabulary = new LinkedHashMap<>();

        if (concepts == null || text == null || text.isBlank()) {
            return vocabulary;
        }

        String[] lines = text.split("\\R");
        List<String> sentences = splitIntoSentences(lines);

        for (String concept : concepts) {
            if (concept == null || concept.isBlank()) {
                continue;
            }
            vocabulary.put(concept, findDefinition(concept, lines, sentences));
        }

        return vocabulary;
    }

    private String findDefinition(String concept, String[] lines, List<String> sentences) {
        String term = termPattern(concept.trim());

        // Tier 1: glossary-style and line-start definitions
        Pattern direct = Pattern.compile("^\\s*" + term + "\\s*" + SEPARATOR + "\\s*(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern lineStart = Pattern.compile("^\\s*" + term + "\\s+" + LINKING_VERBS + "\\s+(.+)$", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            Matcher m = direct.matcher(line);
            if (m.find() && isUsable(m.group(1))) {
                return m.group(1).trim();
            }
            m = lineStart.matcher(line);
            if (m.find() && isUsable(m.group(1)) && !looksPassive(firstSentence(m.group(1)))) {
                return firstSentence(m.group(1));
            }
        }

        // Tier 2: the term as the subject of a sentence, anywhere in a paragraph.
        Pattern subject = Pattern.compile(
                "^(?:(?:a|an|the)\\s+)?" + term + "\\s+" + LINKING_VERBS + "\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern colonForm = Pattern.compile(
                "^(?:(?:a|an|the)\\s+)?" + term + "\\s*" + SEPARATOR + "\\s*(.+)$", Pattern.CASE_INSENSITIVE);

        for (String sentence : sentences) {
            Matcher m = subject.matcher(sentence);
            if (m.find() && isUsable(m.group(1)) && !looksPassive(m.group(1))) {
                return m.group(1).trim();
            }
            m = colonForm.matcher(sentence);
            if (m.find() && isUsable(m.group(1))) {
                return m.group(1).trim();
            }
        }

        // Tier 3: "<definition> is called / is known as <term>"
        Pattern reverse = Pattern.compile(
                "^(.+?)\\s+(?:is\\s+known\\s+as|are\\s+known\\s+as|is\\s+called|are\\s+called)\\s+(?:(?:a|an|the)\\s+)?"
                        + term + "\\b.*$", Pattern.CASE_INSENSITIVE);

        for (String sentence : sentences) {
            Matcher m = reverse.matcher(sentence);
            if (m.find() && isUsable(m.group(1))) {
                return m.group(1).trim();
            }
        }

        return NOT_FOUND;
    }

    /** Splits each line into sentences so a definition in the middle of a paragraph can still be found. */
    private List<String> splitIntoSentences(String[] lines) {
        List<String> sentences = new ArrayList<>();
        for (String line : lines) {
            for (String part : line.trim().split("(?<=[.!?])\\s+")) {
                String s = part.trim();
                if (!s.isEmpty()) {
                    sentences.add(s);
                }
            }
        }
        return sentences;
    }

    /** Matches the term in singular or plural form ("thread" / "threads", "process" / "processes"). */
    private String termPattern(String concept) {
        String lower = concept.toLowerCase();
        String base = lower;
        if (lower.length() > 4 && (lower.endsWith("sses") || lower.endsWith("xes") || lower.endsWith("zes")
                || lower.endsWith("ches") || lower.endsWith("shes"))) {
            base = lower.substring(0, lower.length() - 2);
        } else if (lower.length() > 3 && lower.endsWith("s") && !lower.endsWith("ss")) {
            base = lower.substring(0, lower.length() - 1);
        }
        return Pattern.quote(base) + "(?:es|s)?";
    }

    /** Keeps only the first sentence (a new sentence = end punctuation followed by a capital letter). */
    private String firstSentence(String text) {
        return text.trim().split("(?<=[.!?])\\s+(?=[A-Z])")[0].trim();
    }

    private boolean looksPassive(String definition) {
        String[] words = definition.trim().toLowerCase().split("\\s+");
        if (NON_DEFINITION_STARTS.contains(words[0])) {
            return true;
        }
        return words.length >= 2 && words[0].endsWith("ed") && PASSIVE_FOLLOWERS.contains(words[1]);
    }

    /** Rejects fragments too short to be a real definition. */
    private boolean isUsable(String definition) {
        return definition != null && definition.trim().split("\\s+").length >= 3;
    }
}