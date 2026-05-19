package edu.touro.mcon364.finalreview.model;

public record PrintJob(int id, String documentName, int pages) {
    public PrintJob {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        if (documentName == null || documentName.isBlank()) {
            throw new IllegalArgumentException("documentName must be non-blank");
        }
        if (pages <= 0) throw new IllegalArgumentException("pages must be positive");
    }
}
