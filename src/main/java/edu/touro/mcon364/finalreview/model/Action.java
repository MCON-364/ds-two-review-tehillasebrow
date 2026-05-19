package edu.touro.mcon364.finalreview.model;

/**
 * One action performed in a simple editor.
 */
public record Action(String description, long timestamp) {
    public Action {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must be non-blank");
        }
    }
}
