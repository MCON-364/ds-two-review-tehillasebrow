package edu.touro.mcon364.finalreview.model;

import java.util.List;
import java.util.Map;

public record TicketReport(
        long resolvedCount,
        double averageResolutionMinutes,
        Map<String, Long> countByCategory,
        List<SupportTicket> highPriorityUnresolved
) {
    public TicketReport {
        countByCategory = Map.copyOf(countByCategory);
        highPriorityUnresolved = List.copyOf(highPriorityUnresolved);
    }
}
