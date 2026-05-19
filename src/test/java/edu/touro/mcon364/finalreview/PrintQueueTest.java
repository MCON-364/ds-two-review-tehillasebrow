package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.PrintJob;
import edu.touro.mcon364.finalreview.orderflowhandoff.homework.PrintQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PrintQueueTest {

    private PrintQueue queue;

    @BeforeEach
    void setUp() {
        queue = new PrintQueue();
    }

    private PrintJob job(int id) {
        return new PrintJob(id, "Doc" + id, id);
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void queuedJobsIsZeroInitially() {
        assertEquals(0, queue.queuedJobs());
    }

    @Test
    void printNextReturnsEmptyWhenEmpty() {
        assertEquals(Optional.empty(), queue.printNext());
    }

    @Test
    void peekNextReturnsEmptyWhenEmpty() {
        assertEquals(Optional.empty(), queue.peekNext());
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    void submitIncreasesQueuedJobs() {
        queue.submit(job(1));
        assertEquals(1, queue.queuedJobs());
    }

    @Test
    void submitMultipleJobsIncreasesCount() {
        queue.submit(job(1));
        queue.submit(job(2));
        queue.submit(job(3));
        assertEquals(3, queue.queuedJobs());
    }

    // ── FIFO ordering ─────────────────────────────────────────────────────────

    @Test
    void printNextReturnsFirstSubmittedJobFirst() {
        PrintJob first = job(1);
        PrintJob second = job(2);
        queue.submit(first);
        queue.submit(second);
        assertEquals(Optional.of(first), queue.printNext());
    }

    @Test
    void printNextReturnsJobsInFifoOrder() {
        PrintJob a = job(1);
        PrintJob b = job(2);
        PrintJob c = job(3);
        queue.submit(a);
        queue.submit(b);
        queue.submit(c);
        assertEquals(Optional.of(a), queue.printNext());
        assertEquals(Optional.of(b), queue.printNext());
        assertEquals(Optional.of(c), queue.printNext());
    }

    // ── printNext ─────────────────────────────────────────────────────────────

    @Test
    void printNextRemovesJobFromQueue() {
        queue.submit(job(1));
        queue.printNext();
        assertEquals(0, queue.queuedJobs());
    }

    @Test
    void printNextDecreasesQueuedJobs() {
        queue.submit(job(1));
        queue.submit(job(2));
        queue.printNext();
        assertEquals(1, queue.queuedJobs());
    }

    @Test
    void printNextReturnsEmptyAfterAllJobsRemoved() {
        queue.submit(job(1));
        queue.printNext();
        assertEquals(Optional.empty(), queue.printNext());
    }

    // ── peekNext ──────────────────────────────────────────────────────────────

    @Test
    void peekNextReturnsNextJobWithoutRemoving() {
        PrintJob first = job(1);
        queue.submit(first);
        assertEquals(Optional.of(first), queue.peekNext());
        assertEquals(1, queue.queuedJobs()); // still there
    }

    @Test
    void peekNextDoesNotRemoveJob() {
        queue.submit(job(1));
        queue.peekNext();
        queue.peekNext();
        assertEquals(1, queue.queuedJobs());
    }

    @Test
    void peekNextReturnsSameJobAsNextPrintNext() {
        PrintJob first = job(1);
        queue.submit(first);
        Optional<PrintJob> peeked = queue.peekNext();
        Optional<PrintJob> printed = queue.printNext();
        assertEquals(peeked, printed);
    }

    // ── interleaved submit and print ──────────────────────────────────────────

    @Test
    void interleavedSubmitAndPrint() {
        PrintJob a = job(1);
        PrintJob b = job(2);
        queue.submit(a);
        queue.printNext(); // removes a
        queue.submit(b);
        assertEquals(Optional.of(b), queue.printNext());
    }
}

