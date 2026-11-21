package studyguide;

import java.util.*;

/**
 * Grades a typed flashcard answer against the correct definition.
 *
 * Built from FlashCardGenerator.checkAnswer: the answer is compared by KEY WORDS (filler words ignored),
 * so spacing, punctuation, capitalisation, word order and extra words don't matter, and small typos and
 * word endings (process / processes, execute / execution) still count.
 *
 * The score is the share of the definition's key words that the student covered. At 85% or more the
 * answer gets full credit; below that it is marked wrong, but the score and the missing words are
 * returned so the student can see how close they were.
 */
public final class AnswerGrader {

    /** Share of key words needed for credit. */
    public static final double CREDIT_THRESHOLD = 0.85;
    /** Below the credit line but at least this much: "so close". */
    public static final double CLOSE_THRESHOLD = 0.50;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            // FlashCardGenerator's original list
            "the", "and", "that", "this", "from", "with", "into", "are", "is", "of", "a", "an", "to",
            // more everyday filler, so definitions aren't graded on "in", "its", "which", ...
            "in", "its", "it", "as", "at", "by", "for", "on", "or", "be", "which", "can", "may", "has",
            "have", "had", "their", "they", "them", "one", "any", "each", "than", "then", "also", "was",
            "were", "been", "being", "so", "such", "these", "those", "who", "what", "when", "where"));

    private AnswerGrader() { }

    /** The outcome of grading one answer. */
    public static final class Result {
        private final int percent;
        private final boolean credited;
        private final boolean close;
        private final List<String> matched;
        private final List<String> missed;

        Result(int percent, boolean credited, boolean close, List<String> matched, List<String> missed) {
            this.percent = percent;
            this.credited = credited;
            this.close = close;
            this.matched = matched;
            this.missed = missed;
        }

        /** 0-100: how much of the definition's key words the answer covered. */
        public int getPercent() { return percent; }
        /** True at 85% or more. */
        public boolean isCredited() { return credited; }
        /** True when not credited but at least 50%. */
        public boolean isClose() { return close; }
        public List<String> getMatchedKeywords() { return matched; }
        /** Key words from the correct definition that the answer did not cover. */
        public List<String> getMissedKeywords() { return missed; }
    }

    public static Result grade(String studentAnswer, String correctAnswer) {
        if (correctAnswer == null || correctAnswer.isBlank()) {
            throw new IllegalArgumentException("Cannot evaluate a flashcard without a correct answer.");
        }
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return new Result(0, false, false, new ArrayList<>(), keywordsOf(correctAnswer));
        }

        List<String> correctWords = keywordsOf(correctAnswer);
        Set<String> studentWords = new LinkedHashSet<>(keywordsOf(studentAnswer));

        // Nothing but filler in the definition: fall back to comparing the whole text
        if (correctWords.isEmpty()) {
            boolean same = normalize(studentAnswer).equals(normalize(correctAnswer));
            return new Result(same ? 100 : 0, same, false, new ArrayList<>(), new ArrayList<>());
        }

        List<String> matched = new ArrayList<>();
        List<String> missed = new ArrayList<>();

        for (String correct : correctWords) {
            boolean found = false;
            for (String student : studentWords) {
                if (sameWord(correct, student)) {
                    found = true;
                    break;
                }
            }
            (found ? matched : missed).add(correct);
        }

        double score = (double) matched.size() / correctWords.size();
        int percent = (int) Math.round(score * 100);
        boolean credited = score >= CREDIT_THRESHOLD;
        boolean close = !credited && score >= CLOSE_THRESHOLD;
        return new Result(percent, credited, close, matched, missed);
    }

    // ---------------------------------------------------------------

    /** Key words of a text: lower-case, punctuation and spacing removed, filler dropped, no duplicates. */
    private static List<String> keywordsOf(String text) {
        List<String> words = new ArrayList<>();
        for (String raw : normalize(text).split(" ")) {
            if (raw.length() > 1 && !STOP_WORDS.contains(raw) && !words.contains(raw)) {
                words.add(raw);
            }
        }
        return words;
    }

    private static String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("['\u2019]s\\b", "")      // process's -> process
                .replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** Same word allowing plural / -ed / -ing endings and a small typo in longer words. */
    private static boolean sameWord(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        String sa = stem(a);
        String sb = stem(b);
        if (sa.equals(sb)) {
            return true;
        }
        if (sa.length() >= 5 && sb.length() >= 5) {
            int allowed = (sa.length() >= 9 && sb.length() >= 9) ? 2 : 1;
            return editDistance(sa, sb) <= allowed;
        }
        return false;
    }

    /** Light stemming: threads -> thread, processes -> process, scheduling/scheduled -> schedul, execution/executed -> execut. */
    private static String stem(String word) {
        String w = word;
        if (w.endsWith("sses")) {
            w = w.substring(0, w.length() - 2);
        } else if (w.endsWith("ies") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";
        } else if (w.endsWith("s") && !w.endsWith("ss") && w.length() > 3) {
            w = w.substring(0, w.length() - 1);
        }
        if (w.length() > 5 && w.endsWith("ing")) {
            w = w.substring(0, w.length() - 3);
        } else if (w.length() > 4 && w.endsWith("ed")) {
            w = w.substring(0, w.length() - 2);
        } else if (w.length() > 6 && w.endsWith("ion")) {
            w = w.substring(0, w.length() - 3);   // execution -> execut, same as executed
        }
        return w;
    }

    /** Levenshtein distance (FlashCardGenerator's forgivenessDistance, with its first-column bug fixed). */
    private static int editDistance(String first, String second) {
        int[][] d = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            d[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            d[0][j] = j;
        }
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int cost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i - 1][j - 1] + cost), d[i][j - 1] + 1);
            }
        }
        return d[first.length()][second.length()];
    }
}