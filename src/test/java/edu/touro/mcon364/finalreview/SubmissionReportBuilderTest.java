package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.StudentSubmission;
import edu.touro.mcon364.finalreview.model.SubmissionReport;
import edu.touro.mcon364.finalreview.orderflowhandoff.homework.SubmissionReportBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionReportBuilderTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private StudentSubmission sub(String name, String assignment, int score, boolean late) {
        return new StudentSubmission(name, assignment, score, late);
    }

    // ── constructor ───────────────────────────────────────────────────────────

    @Test
    void constructorThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> new SubmissionReportBuilder(null));
    }

    @Test
    void constructorDefendsAgainstExternalListMutation() {
        List<StudentSubmission> list = new ArrayList<>();
        list.add(sub("Alice", "HW1", 90, false));
        SubmissionReportBuilder builder = new SubmissionReportBuilder(list);
        list.clear();
        assertEquals(1, builder.getSubmissionsByAssignment().get("HW1")); // still there
    }

    // ── empty list ────────────────────────────────────────────────────────────

    @Test
    void lateCountIsZeroForEmptyList() {
        assertEquals(0, new SubmissionReportBuilder(List.of()).getLateCount());
    }

    @Test
    void averageScoreIsZeroForEmptyList() {
        assertEquals(0.0, new SubmissionReportBuilder(List.of()).getAverageScore(), 0.001);
    }

    @Test
    void submissionsByAssignmentIsEmptyForEmptyList() {
        assertTrue(new SubmissionReportBuilder(List.of()).getSubmissionsByAssignment().isEmpty());
    }

    @Test
    void failingSubmissionsIsEmptyForEmptyList() {
        assertTrue(new SubmissionReportBuilder(List.of()).getFailingSubmissions().isEmpty());
    }

    // ── getLateCount ──────────────────────────────────────────────────────────

    @Test
    void lateCountCountsOnlyLateSubmissions() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 85, true),
                sub("Bob", "HW1", 90, false),
                sub("Carol", "HW2", 70, true)
        );
        assertEquals(2, new SubmissionReportBuilder(subs).getLateCount());
    }

    @Test
    void lateCountIsZeroWhenNoneLate() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 85, false),
                sub("Bob", "HW1", 90, false)
        );
        assertEquals(0, new SubmissionReportBuilder(subs).getLateCount());
    }

    // ── getAverageScore ───────────────────────────────────────────────────────

    @Test
    void averageScoreIsCorrect() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 80, false),
                sub("Bob", "HW1", 100, false)
        );
        assertEquals(90.0, new SubmissionReportBuilder(subs).getAverageScore(), 0.001);
    }

    @Test
    void averageScoreForSingleSubmission() {
        List<StudentSubmission> subs = List.of(sub("Alice", "HW1", 75, false));
        assertEquals(75.0, new SubmissionReportBuilder(subs).getAverageScore(), 0.001);
    }

    @Test
    void averageScoreIncludesAllSubmissions() {
        List<StudentSubmission> subs = List.of(
                sub("A", "HW1", 60, false),
                sub("B", "HW1", 70, false),
                sub("C", "HW1", 80, false),
                sub("D", "HW1", 90, false)
        );
        assertEquals(75.0, new SubmissionReportBuilder(subs).getAverageScore(), 0.001);
    }

    // ── getSubmissionsByAssignment ────────────────────────────────────────────

    @Test
    void submissionsByAssignmentGroupsCorrectly() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 80, false),
                sub("Bob", "HW1", 70, false),
                sub("Carol", "HW2", 90, false)
        );
        Map<String, Long> map = new SubmissionReportBuilder(subs).getSubmissionsByAssignment();
        assertEquals(2L, map.get("HW1"));
        assertEquals(1L, map.get("HW2"));
    }

    @Test
    void submissionsByAssignmentIsUnmodifiable() {
        List<StudentSubmission> subs = List.of(sub("Alice", "HW1", 80, false));
        Map<String, Long> map = new SubmissionReportBuilder(subs).getSubmissionsByAssignment();
        assertThrows(Exception.class, () -> map.put("Hacked", 99L));
    }

    // ── getFailingSubmissions ─────────────────────────────────────────────────

    @Test
    void failingSubmissionsReturnsBelowSixty() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 59, false),
                sub("Bob", "HW1", 60, false),
                sub("Carol", "HW1", 45, false),
                sub("Dave", "HW1", 85, false)
        );
        List<StudentSubmission> failing = new SubmissionReportBuilder(subs).getFailingSubmissions();
        assertEquals(2, failing.size());
        assertTrue(failing.stream().allMatch(s -> s.score() < 60));
    }

    @Test
    void failingSubmissionsIsEmptyWhenAllPass() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 60, false),
                sub("Bob", "HW1", 95, false)
        );
        assertTrue(new SubmissionReportBuilder(subs).getFailingSubmissions().isEmpty());
    }

    @Test
    void failingSubmissionsIsUnmodifiable() {
        List<StudentSubmission> subs = List.of(sub("Alice", "HW1", 30, false));
        List<StudentSubmission> failing = new SubmissionReportBuilder(subs).getFailingSubmissions();
        assertThrows(Exception.class, () -> failing.clear());
    }

    // ── buildReport ───────────────────────────────────────────────────────────

    @Test
    void buildReportCombinesAllPieces() {
        List<StudentSubmission> subs = List.of(
                sub("Alice", "HW1", 80, false),
                sub("Bob", "HW1", 55, true),
                sub("Carol", "HW2", 90, true)
        );
        SubmissionReportBuilder builder = new SubmissionReportBuilder(subs);
        SubmissionReport report = builder.buildReport();

        assertEquals(builder.getLateCount(), report.lateCount());
        assertEquals(builder.getAverageScore(), report.averageScore(), 0.001);
        assertEquals(builder.getSubmissionsByAssignment(), report.submissionsByAssignment());
        assertEquals(builder.getFailingSubmissions(), report.failingSubmissions());
    }
}

