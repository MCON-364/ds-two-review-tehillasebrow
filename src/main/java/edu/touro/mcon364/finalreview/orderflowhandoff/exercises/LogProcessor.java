package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;                       // BlockingQueue, LinkedBlockingQueue, TimeUnit
import java.util.concurrent.atomic.AtomicInteger;    // a counter that is safe for many threads

/**
 * LogProcessor — a PRODUCER/CONSUMER system.
 *
 * THREADING 101 (read this once and the whole file makes sense):
 *
 * - A "thread" is a worker that runs code at the same time as other workers.
 *   Normally your program has ONE worker. Here we create several so messages get
 *   processed in parallel.
 *
 * - PRODUCER = whoever calls submit(message). They drop work onto a shared belt.
 * - CONSUMER = the worker threads. They pick work off the belt and process it.
 *
 * - The shared belt is a BlockingQueue. "Blocking" means: if a worker asks for a
 *   message and the belt is empty, the worker politely WAITS instead of spinning.
 *   It is also thread-safe, so many workers can take from it without corrupting it.
 *
 * - The danger with threads: if two of them change the same plain variable at the
 *   same time, the value can come out wrong. So every piece of shared state below
 *   uses a thread-safe tool (AtomicInteger, ConcurrentHashMap, volatile).
 */
public class LogProcessor {

    // The shared belt of work waiting to be processed. Producers offer messages
    // here; consumers take them off. Thread-safe by design.
    private final BlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();

    // A running total of processed messages. AtomicInteger.incrementAndGet() is a
    // single un-interruptible step, so even if 4 workers increment at once, none
    // of the +1's gets lost. A plain `int totalProcessed++` could lose counts.
    private final AtomicInteger totalProcessed = new AtomicInteger();

    // A thread-safe map: LogLevel -> how many of that level we've processed.
    // ConcurrentHashMap lets multiple workers update it at the same time safely.
    private final ConcurrentHashMap<LogLevel, Integer> countsByLevel = new ConcurrentHashMap<>();

    // The worker threads we started, kept so stop() can wait for each to finish.
    private final List<Thread> workers = new ArrayList<>();

    // The on/off switch shared by every worker. `volatile` guarantees that when
    // one thread flips this, the others SEE the new value immediately (without it,
    // a worker might keep reading a stale "true" forever and never stop).
    private volatile boolean running = false;

    /**
     * Accept one message for processing (called by a producer).
     */
    public void submit(LogMessage message) {
        // Only accept work while we're actually running. After stop() flips this
        // off, late submissions are ignored.
        if (running) {
            // offer = add to the back of the belt. Returns immediately.
            queue.offer(message);
        }
    }

    /**
     * Start the requested number of background workers (consumers).
     */
    public void start(int workerCount) {
        // The test requires we reject a non-positive worker count.
        if (workerCount <= 0) {
            throw new IllegalArgumentException("worker count must be positive");
        }
        // Flip the switch ON *before* starting workers so they don't exit instantly.
        running = true;

        // Create exactly workerCount threads. Each runs workerLoop() over and over.
        for (int i = 0; i < workerCount; i++) {
            // `this::workerLoop` means "the code each thread should run is our workerLoop method".
            Thread worker = new Thread(this::workerLoop);
            workers.add(worker);   // remember it so stop() can wait for it
            worker.start();        // actually launch the thread — now it runs in parallel
        }
    }

    /**
     * The work done by ONE background worker. Every worker runs this same loop.
     */
    private void workerLoop() {
        // Keep going while EITHER we're still running OR there is leftover work.
        // The second half is what lets stop() drain the belt: even after running
        // becomes false, workers keep going until the queue is empty.
        while (running || !queue.isEmpty()) {
            try {
                // poll(...) waits up to 100ms for a message. If one arrives, we get it.
                // If nothing arrives in that window, it returns null and we re-check
                // the while-condition (this is how a waiting worker eventually exits).
                LogMessage message = queue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    process(message);
                }
            } catch (InterruptedException e) {
                // Someone asked this thread to stop waiting. Re-flag the interrupt
                // and leave the loop.
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Process one message and update the statistics.
     */
    private void process(LogMessage message) throws InterruptedException {
        // One atomic +1 to the grand total. Safe even with many workers at once.
        totalProcessed.incrementAndGet();
        // merge(key, 1, Integer::sum) means: if this level isn't in the map, put 1;
        // if it's already there, add 1 to the existing value. Done atomically by
        // ConcurrentHashMap, so concurrent updates to the same level stay correct.
        countsByLevel.merge(message.level(), 1, Integer::sum);
    }

    /**
     * Stop the processor and WAIT for every worker to finish leftover work.
     */
    public void stop() throws InterruptedException {
        // Flip the switch OFF. Workers will finish the belt, then exit their loops.
        running = false;
        // join() blocks the caller until that worker's loop has fully ended.
        // Looping over all workers means stop() only returns once ALL of them are
        // done — guaranteeing every submitted message was processed.
        for (Thread worker : workers) {
            worker.join();
        }
        // Clear the list so a second stop() call (the test's tearDown does this) is harmless.
        workers.clear();
    }

    /**
     * Return the number of messages processed so far.
     */
    public int getTotalProcessed() {
        // .get() reads the current value of the atomic counter.
        return totalProcessed.get();
    }

    /**
     * Return a SAFE snapshot of the counts by level.
     */
    public Map<LogLevel, Integer> getCountsByLevel() {
        // Map.copyOf makes an immutable copy. Callers can read it but cannot put()
        // into it (that throws), so they can never corrupt our internal counts.
        return Map.copyOf(countsByLevel);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
