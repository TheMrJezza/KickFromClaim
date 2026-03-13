package dev.themrjezza.kickfromclaim.pdf;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Extracts searchable text from PDF files, including scanned (image-only) documents.
 *
 * <p>For PDFs that contain embedded text the text is extracted directly via PDFBox.
 * For scanned documents (pages composed entirely of images), each page is rendered
 * at the configured DPI and processed with Tesseract OCR so that the resulting text
 * is fully searchable.</p>
 *
 * <p>A status callback is invoked at the start of extraction so callers can surface
 * a message such as {@code "Extracting text from PDF..."} to the end user.</p>
 *
 * <p><b>Tesseract setup:</b> OCR requires Tesseract data files on the host system.
 * By default Tess4J looks for them in the {@code TESSDATA_PREFIX} environment variable
 * or the standard system location (e.g. {@code /usr/share/tesseract-ocr/4.00/tessdata}
 * on Linux).  Use the {@link #PdfTextExtractor(Tesseract, int)} constructor to supply a
 * pre-configured {@link Tesseract} instance with a custom data path and language.</p>
 */
public class PdfTextExtractor {

    /** Default resolution used when rendering PDF pages for OCR. */
    public static final int DEFAULT_RENDER_DPI = 300;

    private final int renderDpi;
    private final Tesseract tesseract;

    /**
     * Creates an extractor with default settings (300 DPI rendering, system Tesseract config).
     */
    public PdfTextExtractor() {
        this(new Tesseract(), DEFAULT_RENDER_DPI);
    }

    /**
     * Creates an extractor that uses the supplied {@link Tesseract} instance and DPI value.
     *
     * <p>Use this constructor to customise the Tesseract data path / language settings or
     * to lower the DPI when processing large documents to reduce memory consumption.
     * Higher DPI values improve OCR accuracy but increase memory usage proportionally.</p>
     *
     * @param tesseract pre-configured Tesseract instance
     * @param renderDpi dots-per-inch used when rendering PDF pages for OCR; typical values
     *                  are 150 (faster, lower accuracy) to 300 (recommended default)
     */
    public PdfTextExtractor(@NotNull Tesseract tesseract, int renderDpi) {
        this.tesseract = tesseract;
        this.renderDpi = renderDpi;
    }

    /**
     * Extracts text from the given PDF file.
     *
     * <p>The {@code statusCallback} is invoked with a human-readable status string
     * immediately before each phase of extraction so that callers can relay
     * progress to the user (e.g. {@code "Extracting text from PDF..."}).</p>
     *
     * @param pdfFile        the PDF file to process; must not be {@code null}
     * @param statusCallback receives status messages during extraction; must not be {@code null}
     * @return the full extracted text, possibly empty but never {@code null}
     * @throws IOException if the file cannot be read or OCR encounters a fatal error
     */
    @NotNull
    public String extractText(@NotNull File pdfFile, @NotNull Consumer<String> statusCallback) throws IOException {
        statusCallback.accept("Extracting text from PDF...");

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text != null && !text.isBlank()) {
                return text;
            }

            // No embedded text found – this is likely a scanned document; use OCR.
            return performOcr(document, statusCallback);
        }
    }

    /**
     * Renders each page of {@code document} and runs Tesseract OCR on the result.
     *
     * <p>Pages are processed one at a time so that rendered images can be garbage-collected
     * before the next page is rendered, keeping peak memory consumption bounded even for
     * large documents.</p>
     *
     * @param document       an already-opened {@link PDDocument}
     * @param statusCallback receives status updates
     * @return OCR text for all pages, with pages separated by a form-feed character
     * @throws IOException if rendering or OCR fails
     */
    @NotNull
    private String performOcr(@NotNull PDDocument document, @NotNull Consumer<String> statusCallback) throws IOException {
        statusCallback.accept("Extracting text from PDF (OCR)...");

        PDFRenderer renderer = new PDFRenderer(document);
        StringBuilder ocrText = new StringBuilder();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            // Render and OCR one page at a time so the BufferedImage can be GC'd before
            // the next iteration, keeping memory pressure manageable for large documents.
            BufferedImage image = renderer.renderImageWithDPI(page, renderDpi);
            try {
                if (ocrText.length() > 0) {
                    ocrText.append('\f'); // form-feed as page separator
                }
                ocrText.append(tesseract.doOCR(image));
            } catch (TesseractException e) {
                throw new IOException("OCR failed on page " + (page + 1), e);
            }
        }

        return ocrText.toString();
    }
}
