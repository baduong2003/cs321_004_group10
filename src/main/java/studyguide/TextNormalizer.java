package studyguide;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Cleans text extracted from PDFs / text files so that materials of different kinds
 * (textbook, notes, slides) can be combined and analysed together.
 *
 * Steps: unify characters (ligatures, curly quotes, odd spaces), drop page numbers,
 * drop headers/footers that repeat on many pages, then rejoin lines that the PDF
 * wrapped in the middle of a sentence (and words hyphenated across a line break).
 *
 * Pages are separated by a form feed (\f), which MaterialParser inserts for PDFs.
 */
public final class TextNormalizer {

    private static final Pattern PAGE_NUMBER_LINE = Pattern.compile(
            "^(?:page\\s+)?\\d{1,4}(?:\\s*(?:of|/)\\s*\\d{1,4})?$", Pattern.CASE_INSENSITIVE);

    private TextNormalizer() { }

    /** Full cleanup. Set unwrap=false for text that already has one sentence per line. */
    public static String normalize(String raw, boolean unwrap) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String text = normalizeCharacters(raw);
        String[] pages = text.split("\f", -1);

        // 1. Find short lines that repeat on many different pages (running headers / footers).
        Set<String> repeated = findRepeatedLines(pages);

        // 2. Rebuild, dropping page numbers and repeated headers/footers.
        StringBuilder out = new StringBuilder();
        for (String page : pages) {
            for (String line : page.split("\n", -1)) {
                String trimmed = line.strip();
                if (!trimmed.isEmpty()) {
                    if (PAGE_NUMBER_LINE.matcher(trimmed).matches()) {
                        continue;
                    }
                    if (repeated.contains(signature(trimmed))) {
                        continue;
                    }
                }
                out.append(trimmed).append('\n');
            }
            out.append('\n'); // a page break is also a paragraph break
        }

        String cleaned = out.toString();
        if (unwrap) {
            cleaned = MaterialParser.unwrapLines(cleaned);
        }
        // Slide / list bullets ("- Mutex: a lock...") hide the term from the matcher, so drop the marker.
        // (Done after unwrapping, so a bullet is never glued onto the line before it.)
        cleaned = cleaned.replaceAll("(?m)^[-*]\\s+", "");
        // Collapse runs of blank lines
        return cleaned.replaceAll("\n{3,}", "\n\n").strip();
    }

    static String normalizeCharacters(String s) {
        return s.replace("\r\n", "\n").replace('\r', '\n')
                .replace("\uFB00", "ff").replace("\uFB01", "fi").replace("\uFB02", "fl")
                .replace("\uFB03", "ffi").replace("\uFB04", "ffl")
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201C', '"').replace('\u201D', '"')
                .replace('\u00A0', ' ').replace("\u00AD", "")
                .replace("\u2022", "-")   // bullet glyphs
                .replace('\t', ' ')
                .replaceAll("[ ]{2,}", " ");
    }

    /** A line with digits blanked out, so "Chapter 3  45" and "Chapter 3  46" count as the same header. */
    private static String signature(String line) {
        return line.replaceAll("\\d+", "#").toLowerCase();
    }

    private static Set<String> findRepeatedLines(String[] pages) {
        Set<String> repeated = new HashSet<>();
        if (pages.length < 3) {
            return repeated; // too few pages to tell a header from real content
        }
        Map<String, Integer> pageCounts = new HashMap<>();
        for (String page : pages) {
            Set<String> seenOnThisPage = new HashSet<>();
            for (String line : page.split("\n")) {
                String t = line.strip();
                if (!t.isEmpty() && t.length() <= 80 && !PAGE_NUMBER_LINE.matcher(t).matches()) {
                    seenOnThisPage.add(signature(t));
                }
            }
            for (String sig : seenOnThisPage) {
                pageCounts.merge(sig, 1, Integer::sum);
            }
        }
        int threshold = Math.max(3, (int) Math.ceil(pages.length * 0.5));
        for (Map.Entry<String, Integer> e : pageCounts.entrySet()) {
            if (e.getValue() >= threshold) {
                repeated.add(e.getKey());
            }
        }
        return repeated;
    }
}