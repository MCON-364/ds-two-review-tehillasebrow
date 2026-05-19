package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.Priority;
import edu.touro.mcon364.finalreview.model.SupportTicket;
import edu.touro.mcon364.finalreview.model.TicketReport;
import edu.touro.mcon364.finalreview.orderflowhandoff.exercises.TicketReportBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TicketReportBuilderTest {

    // ── helpers ───────────────────────────────────────────────────────────────

    private SupportTicket resolved(int id, String category, Priority priority, int minutes) {
        return new SupportTicket(id, category, priority, minutes, true);
    }

    private SupportTicket unresolved(int id, String category, Priority priority) {
        return new SupportTicket(id, category, priority, 0, false);
    }

    // ── constructor ───────────────────────────────────────────────────────────

    @Test
    void constructorThrowsOnNullList() {
        assertThrows(Exception.class, () -> new TicketReportBuilder(null));
    }

    @Test
    void constructorDefendsAgainstExternalMutation() {
        List<SupportTicket> tickets = new ArrayList<>();
        tickets.add(resolved(1, "Billing", Priority.HIGH, 30));
        TicketReportBuilder builder = new TicketReportBuilder(tickets);
        tickets.clear();
        assertEquals(1, builder.getResolvedCount()); // internal copy unaffected
    }

    // ── empty list ────────────────────────────────────────────────────────────

    @Test
    void resolvedCountIsZeroForEmptyList() {
        assertEquals(0, new TicketReportBuilder(List.of()).getResolvedCount());
    }

    @Test
    void averageResolutionMinutesIsZeroForEmptyList() {
        assertEquals(0.0, new TicketReportBuilder(List.of()).getAverageResolutionMinutes());
    }

    @Test
    void countByCategoryIsEmptyForEmptyList() {
        assertTrue(new TicketReportBuilder(List.of()).getCountByCategory().isEmpty());
    }

    @Test
    void highPriorityUnresolvedIsEmptyForEmptyList() {
        assertTrue(new TicketReportBuilder(List.of()).getHighPriorityUnresolved().isEmpty());
    }

    // ── getResolvedCount ──────────────────────────────────────────────────────

    @Test
    void resolvedCountCountsOnlyResolvedTickets() {
        List<SupportTicket> tickets = List.of(
                resolved(1, "Billing", Priority.LOW, 10),
                resolved(2, "Tech", Priority.LOW, 20),
                unresolved(3, "Tech", Priority.MEDIUM)
        );
        assertEquals(2, new TicketReportBuilder(tickets).getResolvedCount());
    }

    @Test
    void resolvedCountIsZeroWhenAllUnresolved() {
        List<SupportTicket> tickets = List.of(
                unresolved(1, "Billing", Priority.LOW),
                unresolved(2, "Tech", Priority.MEDIUM)
        );
        assertEquals(0, new TicketReportBuilder(tickets).getResolvedCount());
    }

    // ── getAverageResolutionMinutes ────────────────────────────────────────────

    @Test
    void averageResolutionExcludesUnresolvedTickets() {
        List<SupportTicket> tickets = List.of(
                resolved(1, "Billing", Priority.LOW, 30),
                resolved(2, "Tech", Priority.LOW, 60),
                unresolved(3, "Tech", Priority.HIGH)
        );
        assertEquals(45.0, new TicketReportBuilder(tickets).getAverageResolutionMinutes(), 0.001);
    }

    @Test
    void averageResolutionIsZeroWhenNoResolvedTickets() {
        List<SupportTicket> tickets = List.of(unresolved(1, "Tech", Priority.MEDIUM));
        assertEquals(0.0, new TicketReportBuilder(tickets).getAverageResolutionMinutes(), 0.001);
    }

    @Test
    void averageResolutionIsExactForSingleTicket() {
        List<SupportTicket> tickets = List.of(resolved(1, "Billing", Priority.LOW, 120));
        assertEquals(120.0, new TicketReportBuilder(tickets).getAverageResolutionMinutes(), 0.001);
    }

    // ── getCountByCategory ────────────────────────────────────────────────────

    @Test
    void countByCategoryGroupsCorrectly() {
        List<SupportTicket> tickets = List.of(
                resolved(1, "Billing", Priority.LOW, 10),
                unresolved(2, "Billing", Priority.MEDIUM),
                resolved(3, "Tech", Priority.LOW, 20)
        );
        Map<String, Long> counts = new TicketReportBuilder(tickets).getCountByCategory();
        assertEquals(2L, counts.get("Billing"));
        assertEquals(1L, counts.get("Tech"));
    }

    @Test
    void countByCategoryIsUnmodifiable() {
        List<SupportTicket> tickets = List.of(resolved(1, "Billing", Priority.LOW, 10));
        Map<String, Long> counts = new TicketReportBuilder(tickets).getCountByCategory();
        assertThrows(Exception.class, () -> counts.put("Hacked", 99L));
    }

    // ── getHighPriorityUnresolved ──────────────────────────────────────────────

    @Test
    void highPriorityUnresolvedReturnsOnlyHighPriorityUnresolvedTickets() {
        List<SupportTicket> tickets = List.of(
                unresolved(1, "Billing", Priority.HIGH),
                unresolved(2, "Tech", Priority.MEDIUM),
                resolved(3, "Tech", Priority.HIGH, 20),
                unresolved(4, "HR", Priority.HIGH)
        );
        List<SupportTicket> result = new TicketReportBuilder(tickets).getHighPriorityUnresolved();
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.priority() == Priority.HIGH && !t.resolved()));
    }

    @Test
    void highPriorityUnresolvedIsUnmodifiable() {
        List<SupportTicket> tickets = List.of(unresolved(1, "Billing", Priority.HIGH));
        List<SupportTicket> result = new TicketReportBuilder(tickets).getHighPriorityUnresolved();
        assertThrows(Exception.class, () -> result.clear());
    }

    @Test
    void highPriorityUnresolvedIsEmptyWhenNone() {
        List<SupportTicket> tickets = List.of(
                unresolved(1, "Billing", Priority.LOW),
                resolved(2, "Tech", Priority.HIGH, 10)
        );
        assertTrue(new TicketReportBuilder(tickets).getHighPriorityUnresolved().isEmpty());
    }

    // ── buildReport ───────────────────────────────────────────────────────────

    @Test
    void buildReportCombinesAllPieces() {
        List<SupportTicket> tickets = List.of(
                resolved(1, "Billing", Priority.LOW, 40),
                resolved(2, "Billing", Priority.MEDIUM, 60),
                unresolved(3, "Tech", Priority.HIGH),
                unresolved(4, "Tech", Priority.LOW)
        );
        TicketReportBuilder builder = new TicketReportBuilder(tickets);
        TicketReport report = builder.buildReport();

        assertEquals(builder.getResolvedCount(), report.resolvedCount());
        assertEquals(builder.getAverageResolutionMinutes(), report.averageResolutionMinutes(), 0.001);
        assertEquals(builder.getCountByCategory(), report.countByCategory());
        assertEquals(builder.getHighPriorityUnresolved(), report.highPriorityUnresolved());
    }
}

