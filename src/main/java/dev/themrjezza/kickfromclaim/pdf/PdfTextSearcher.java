package dev.themrjezza.kickfromclaim.pdf;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Searches within text that has been extracted from a PDF document.
 *
 * <p>Searches are case-insensitive by default. The searcher is immutable once constructed;
 * obtain a fresh instance via {@link PdfTextExtractor#extractAndSearch} to search updated content.
 *
 * <p>Usage example:
 * <pre>{@code
 * PdfTextExtractor extractor = new PdfTextExtractor(msg -> System.out.println(msg));
 * PdfTextSearcher searcher = extractor.extractAndSearch(new File("document.pdf"));
 *
 * if (searcher.contains("invoice")) {
 *     List<String> lines = searcher.findLines("invoice");
 *     lines.forEach(System.out::println);
 * }
 * }</pre>
 */
public class PdfTextSearcher {

    private final String text;

    /**
     * Creates a new {@code PdfTextSearcher} backed by the supplied text.
     *
     * @param text the text to search; must not be {@code null}
     */
    public PdfTextSearcher(@NotNull String text) {
        this.text = text;
    }

    /**
     * Returns {@code true} if the extracted text contains the given query (case-insensitive).
     *
     * @param query the string to search for
     * @return {@code true} if the query is found
     */
    public boolean contains(@NotNull String query) {
        return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns all lines from the extracted text that contain the given query (case-insensitive).
     *
     * @param query the string to search for
     * @return a list of matching lines (never {@code null}, possibly empty)
     */
    @NotNull
    public List<String> findLines(@NotNull String query) {
        final String lowerQuery = query.toLowerCase(Locale.ROOT);
        final List<String> matches = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (line.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                matches.add(line);
            }
        }
        return matches;
    }

    /**
     * Returns the number of times the query appears in the extracted text (case-insensitive).
     *
     * @param query the string to count
     * @return the number of occurrences
     */
    public int countOccurrences(@NotNull String query) {
        if (query.isEmpty()) return 0;
        final Matcher matcher = Pattern.compile(
                Pattern.quote(query), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        ).matcher(text);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    /**
     * Returns the full extracted text that this searcher operates on.
     *
     * @return the extracted text
     */
    @NotNull
    public String getText() {
        return text;
    }

    /**
     * Returns {@code true} if no text was extracted (e.g. the PDF was a scanned image
     * without a text layer).
     *
     * @return {@code true} when the extracted text is empty
     */
    public boolean isEmpty() {
        return text.isEmpty();
    }
}
