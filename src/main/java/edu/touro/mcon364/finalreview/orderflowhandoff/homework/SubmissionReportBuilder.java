package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.StudentSubmission;
import edu.touro.mcon364.finalreview.model.SubmissionReport;

import java.util.List;
import java.util.Map;
import java.util.Objects;
// Collectors holds the "recipes" for turning a stream into a collection
// (a list, a map, a count, an average, etc.).
import java.util.stream.Collectors;

/**
 * Homework 3 — Building a report from a completed collection.
 *
 * A gradebook already contains a list of assignment submissions. Each submission
 * represents one student's work for one assignment. At this point, the data is
 * not changing while the report is being built. Nothing is being produced by one
 * thread and consumed by another thread. We are simply analyzing a collection
 * that already exists.
 *
 * The job of this class is to answer several reporting questions about that
 * collection and then combine those answers into one SubmissionReport.
 *
 * Before coding, think through the shape of the problem:
 * - What information is already available in each StudentSubmission?
 * - Which questions require counting?
 * - Which questions require calculating a numeric summary?
 * - Which questions require grouping submissions by one field?
 * - Which questions require selecting only some submissions?
 * - Since the input list is already complete, do we need threads here?
 *
 * Requirements:
 * - The constructor receives the submissions that will be analyzed.
 * - The builder must not expose or mutate its internal list of submissions.
 * - getLateCount() returns how many submissions were marked late.
 * - getAverageScore() returns the average score across all submissions.
 * - getSubmissionsByAssignment() returns how many submissions exist for each assignment name.
 * - getFailingSubmissions() returns submissions whose score is below 60.
 * - buildReport() returns a SubmissionReport containing all four pieces of information.
 *
 * Edge cases to consider:
 * - An empty submission list should not cause a crash.
 * - A caller should not be able to change this builder's internal state by
 *   modifying the original list after construction.
 * - Returned collections should not allow callers to mutate the builder's
 *   internal state.
 */
public class SubmissionReportBuilder {

    private final List<StudentSubmission> submissions;

    public SubmissionReportBuilder(List<StudentSubmission> submissions) {
        this.submissions = List.copyOf(Objects.requireNonNull(submissions));
    }

    /**
     * Return the number of submissions that were turned in late.
     *
     * STREAMS 101: `submissions.stream()` turns the list into a "conveyor belt"
     * of items that we can run operations on one-by-one. Nothing is changed in
     * the original list — a stream just *reads* and produces an answer.
     */
    public long getLateCount() {
        return submissions.stream()           // start the conveyor belt of submissions
                // filter = keep only the items where the test is true.
                // StudentSubmission::late is shorthand for "for each submission s, call s.late()".
                // s.late() returns true if that submission was turned in late.
                .filter(StudentSubmission::late)
                // count = how many items survived the filter. Returns a long.
                .count();
    }

    /**
     * Return the average score across all submissions.
     * If there are no submissions, return 0.0.
     */
    public double getAverageScore() {
        return submissions.stream()
                // mapToInt = convert each submission into just its number (its score).
                // The belt now carries ints (the scores) instead of whole submissions.
                // s -> s.score() means "for each submission s, give me s.score()".
                .mapToInt(s -> s.score())
                // average = the mean of those ints. It returns an OptionalDouble
                // (a maybe-empty box) because an EMPTY list has no average.
                .average()
                // orElse(0.0) = "if the box is empty (no submissions), use 0.0 instead."
                // This satisfies the requirement: empty list -> 0.0, no crash.
                .orElse(0.0);
    }

    /**
     * Return a map: assignment name -> how many submissions it received.
     */
    public Map<String, Long> getSubmissionsByAssignment() {
        return Map.copyOf(                     // (last step) freeze the map so callers can't edit it
                submissions.stream()
                        // groupingBy makes a Map. The first argument decides the KEY for each item;
                        // here every submission is filed under its assignmentName ("HW1", "HW2"...).
                        // Collectors.counting() decides the VALUE: count how many items fell into
                        // each group. Result: {"HW1" -> 2, "HW2" -> 1}.
                        .collect(Collectors.groupingBy(
                                s -> s.assignmentName(),
                                Collectors.counting()))
        );
    }

    /**
     * Return the submissions whose score is below 60.
     */
    public List<StudentSubmission> getFailingSubmissions() {
        return submissions.stream()
                // keep only submissions whose score is under 60.
                .filter(s -> s.score() < 60)
                // toList() gathers the survivors into a List. In Java 16+ this list
                // is UNMODIFIABLE, so callers can't sneak in changes to our data.
                .toList();
    }

    /**
     * Build the complete report by combining the smaller reporting questions.
     */
    public SubmissionReport buildReport() {
        return new SubmissionReport(
                getLateCount(),
                getAverageScore(),
                getSubmissionsByAssignment(),
                getFailingSubmissions()
        );
    }
}
