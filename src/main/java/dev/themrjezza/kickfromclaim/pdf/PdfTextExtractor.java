package dev.themrjezza.kickfromclaim.pdf;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts text from PDF documents, including scanned PDFs.
 *
 * <p>For digital PDFs (those with embedded text), text is extracted directly.
 * For scanned PDFs (image-only pages where no text layer is present), the
 * extractor detects the condition and reports it via the progress listener.
 *
 * <p>Usage example:
 * <pre>{@code
 * PdfTextExtractor extractor = new PdfTextExtractor(message -> System.out.println(message));
 * String text = extractor.extractText(new File("document.pdf"));
 * }</pre>
 */
public class PdfTextExtractor {

    private static final String MSG_STARTING = "Extracting text from PDF...";
    private static final String MSG_PAGE = "Extracting text from page %d of %d...";
    private static final String MSG_DONE = "Text extraction complete.";
    private static final String MSG_SCANNED =
            "No text layer found – this appears to be a scanned document. OCR is required to extract text.";

    private final ExtractionProgressListener progressListener;

    /**
     * Creates a new {@code PdfTextExtractor} with the given progress listener.
     *
     * @param progressListener listener that receives status messages during extraction
     */
    public PdfTextExtractor(@NotNull ExtractionProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    /**
     * Extracts all text from the given PDF file.
     *
     * @param file the PDF file to process
     * @return the extracted text, or an empty string if no text could be found
     * @throws IOException if the file cannot be read or is not a valid PDF
     */
    @NotNull
    public String extractText(@NotNull File file) throws IOException {
        progressListener.onProgressUpdate(MSG_STARTING);
        try (PDDocument document = Loader.loadPDF(file)) {
            return extractFromDocument(document);
        }
    }

    /**
     * Extracts all text from a PDF supplied as an input stream.
     *
     * @param inputStream the input stream containing the PDF bytes
     * @return the extracted text, or an empty string if no text could be found
     * @throws IOException if the stream cannot be read or does not contain a valid PDF
     */
    @NotNull
    public String extractText(@NotNull InputStream inputStream) throws IOException {
        progressListener.onProgressUpdate(MSG_STARTING);
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            return extractFromDocument(document);
        }
    }

    private @NotNull String extractFromDocument(@NotNull PDDocument document) throws IOException {
        final int pageCount = document.getNumberOfPages();
        final PDFTextStripper stripper = new PDFTextStripper();
        final StringBuilder result = new StringBuilder();

        // Extract page-by-page so progress can be reported for each page.
        for (int page = 1; page <= pageCount; page++) {
            progressListener.onProgressUpdate(String.format(MSG_PAGE, page, pageCount));
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            result.append(stripper.getText(document));
        }

        final String text = result.toString().strip();
        if (text.isEmpty()) {
            progressListener.onProgressUpdate(MSG_SCANNED);
            return "";
        }

        progressListener.onProgressUpdate(MSG_DONE);
        return text;
    }

    /**
     * Returns a {@link PdfTextSearcher} pre-loaded with text extracted from the given file.
     *
     * <p>This is a convenience method that combines extraction and searcher creation in one call:
     * <pre>{@code
     * PdfTextSearcher searcher = extractor.extractAndSearch(pdfFile);
     * boolean found = searcher.contains("invoice");
     * }</pre>
     *
     * @param file the PDF file to process
     * @return a searcher over the extracted text
     * @throws IOException if the file cannot be read or is not a valid PDF
     */
    @NotNull
    public PdfTextSearcher extractAndSearch(@NotNull File file) throws IOException {
        return new PdfTextSearcher(extractText(file));
    }

    /**
     * Returns a {@link PdfTextSearcher} pre-loaded with text extracted from the given stream.
     *
     * @param inputStream the input stream containing the PDF bytes
     * @return a searcher over the extracted text
     * @throws IOException if the stream cannot be read or does not contain a valid PDF
     */
    @NotNull
    public PdfTextSearcher extractAndSearch(@NotNull InputStream inputStream) throws IOException {
        return new PdfTextSearcher(extractText(inputStream));
    }

    /**
     * Returns the {@link ExtractionProgressListener} used by this extractor.
     *
     * @return the progress listener
     */
    @NotNull
    public ExtractionProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * Creates a new {@code PdfTextExtractor} with a no-op progress listener.
     *
     * @return extractor with no-op listener
     */
    @NotNull
    public static PdfTextExtractor silent() {
        return new PdfTextExtractor(message -> { });
    }
}
