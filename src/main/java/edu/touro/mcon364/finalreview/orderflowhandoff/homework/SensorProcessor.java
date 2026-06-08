package edu.touro.mcon364.finalreview.orderflowhandoff.homework;

import edu.touro.mcon364.finalreview.model.SensorReading;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Homework 2 — SensorProcessor.
 *
 * This is the SAME producer/consumer pattern as LogProcessor (see that file's
 * header for the full threading explanation). The only twist: instead of counting
 * messages by level, we keep running NUMBER statistics (count, min, max, sum, average)
 * using a built-in helper called DoubleSummaryStatistics.
 *
 * - PRODUCER: whoever calls submit(reading) drops a reading on the shared belt.
 * - CONSUMER: worker threads take readings off the belt and feed each value into
 *   our statistics object.
 */
public class SensorProcessor {

    // The shared, thread-safe belt of readings waiting to be processed.
    private final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>();

    // The worker threads, kept so stop() can wait for them to finish.
    private final List<Thread> workers = new ArrayList<>();

    // The shared on/off switch. volatile = every thread sees changes immediately.
    private volatile boolean running = false;

    // Thread-safe count of how many readings we've processed.
    private final AtomicInteger totalProcessed = new AtomicInteger();

    // Built-in accumulator: every time you call .accept(number) it updates the
    // count, min, max, sum, and average for you. It is NOT thread-safe on its own,
    // so below we wrap every use of it in `synchronized (stats)` (explained there).
    private final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

    /**
     * Accept one sensor reading for processing (called by a producer).
     */
    public void submit(SensorReading reading) {
        if (running) {
            queue.offer(reading);   // add to the back of the belt
        }
    }

    /**
     * Start the requested number of background workers (consumers).
     */
    public void start(int workerCount) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("workerCount must be positive");
        }
        running = true;   // turn on BEFORE launching so workers don't exit instantly
        for (int i = 0; i < workerCount; i++) {
            Thread worker = new Thread(this::workerLoop);
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * Logic run by each worker thread.
     */
    private void workerLoop() {
        // Keep working while running, OR while there is still leftover work to drain.
        while (running || !queue.isEmpty()) {
            try {
                // Wait up to 100ms for a reading; null means "nothing arrived, loop again".
                SensorReading reading = queue.poll(100, TimeUnit.MILLISECONDS);
                if (reading != null) {
                    process(reading);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Process one reading: bump the count and fold its value into the statistics.
     */
    private void process(SensorReading reading) {
        totalProcessed.incrementAndGet();   // safe +1 across all workers

        // synchronized (stats) = "only one thread at a time may run this block."
        // DoubleSummaryStatistics.accept() is several internal steps; if two workers
        // ran it at once the numbers could come out wrong. The lock forces them to
        // take turns, keeping min/max/sum/count correct.
        synchronized (stats) {
            stats.accept(reading.value());   // update count/min/max/sum/average with this value
        }
    }

    /**
     * Stop the processor and wait for workers to finish the remaining work.
     */
    public void stop() throws InterruptedException {
        running = false;                 // tell workers to wind down
        for (Thread worker : workers) {
            worker.join();               // wait until each worker's loop fully ends
        }
        workers.clear();                 // makes a second stop() call harmless
    }

    /**
     * Return the number of readings processed so far.
     */
    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    /**
     * Return summary statistics for the processed values — as a safe COPY, so a
     * caller can't reach into and mutate our live, shared `stats` object.
     */
    public DoubleSummaryStatistics getStats() {
        DoubleSummaryStatistics snapshot = new DoubleSummaryStatistics();
        // Lock so we read a consistent picture while workers might be updating it,
        // then copy everything from the live stats into our fresh snapshot.
        synchronized (stats) {
            snapshot.combine(stats);     // combine = merge the live totals into the copy
        }
        return snapshot;                 // caller gets the copy; our internal stats stays private
    }
}
