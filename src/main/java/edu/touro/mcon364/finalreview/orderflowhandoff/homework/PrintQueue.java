package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.PrintJob;

// ArrayDeque is a "double-ended queue" — you can add/remove from either end.
// We use it here as a normal FIFO (First-In-First-Out) queue: first job in, first job out.
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Homework 1 — PrintQueue.
 *
 * THE BIG IDEA: a printer line behaves like a line at a store.
 * The FIRST person to get in line is the FIRST person served. That rule is
 * called FIFO (First In, First Out). The data structure that gives us FIFO is a QUEUE.
 *
 * - submit  = a new job walks up and gets in the BACK of the line.
 * - printNext = the printer serves the person at the FRONT of the line (and they leave).
 * - peekNext = the printer LOOKS at who is at the front, but doesn't serve them yet.
 * - queuedJobs = how many people are currently waiting in line.
 */
public class PrintQueue {

    // This is the ONE thing this object has to remember between method calls:
    // the line of waiting jobs. `Deque` is the type (the "shape"); `ArrayDeque`
    // is the concrete implementation we picked. `final` means we will never point
    // `jobs` at a *different* queue object — but we can still add/remove items inside it.
    private final Deque<PrintJob> jobs = new ArrayDeque<>();

    /**
     * Records a new print job as waiting.
     */
    public void submit(PrintJob job) {
        // addLast = put this job at the BACK of the line (the tail).
        // That is what makes it "fair": newcomers go to the end.
        jobs.addLast(job);
    }

    /**
     * Removes and returns the print job that should be handled next.
     */
    public Optional<PrintJob> printNext() {
        // pollFirst = take the job at the FRONT of the line AND remove it.
        // If the line is empty, pollFirst returns null instead of crashing.
        PrintJob next = jobs.pollFirst();

        // We never hand `null` back to the caller. Instead we wrap the result in an
        // Optional: a little box that either contains a job (Optional.of) or is
        // empty (Optional.empty). ofNullable picks the right one automatically:
        // if `next` is null -> empty box; otherwise -> box holding the job.
        return Optional.ofNullable(next);
    }

    /**
     * Returns the print job that would be handled next WITHOUT removing it.
     */
    public Optional<PrintJob> peekNext() {
        // peekFirst = LOOK at the front job but leave it in the line.
        // Returns null if the line is empty.
        PrintJob next = jobs.peekFirst();
        return Optional.ofNullable(next);
    }

    /**
     * Returns the number of jobs currently waiting to be printed.
     */
    public int queuedJobs() {
        // size() = how many items are in the line right now.
        return jobs.size();
    }
}
