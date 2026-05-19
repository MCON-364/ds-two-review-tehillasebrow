package edu.touro.mcon364.finalreview.model;

import java.util.List;
import java.util.Map;

public record SubmissionReport(
        long lateCount,
        double averageScore,
        Map<String, Long> submissionsByAssignment,
        List<StudentSubmission> failingSubmissions
) {
    public SubmissionReport {
        submissionsByAssignment = Map.copyOf(submissionsByAssignment);
        failingSubmissions = List.copyOf(failingSubmissions);
    }
}
