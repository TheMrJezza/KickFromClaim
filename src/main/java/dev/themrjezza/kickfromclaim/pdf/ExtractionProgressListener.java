package dev.themrjezza.kickfromclaim.pdf;

/**
 * Listener that receives progress updates during PDF text extraction.
 */
@FunctionalInterface
public interface ExtractionProgressListener {

    /**
     * Called when there is a progress update during extraction.
     *
     * @param message a human-readable status message, e.g. "Extracting text from PDF..."
     */
    void onProgressUpdate(String message);
}
