package edu.touro.mcon364.finalreview.model;

public record StudentSubmission(
        String studentName,
        String assignmentName,
        int score,
        boolean late
) {
    public StudentSubmission {
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("studentName must be non-blank");
        }
        if (assignmentName == null || assignmentName.isBlank()) {
            throw new IllegalArgumentException("assignmentName must be non-blank");
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
    }
}
