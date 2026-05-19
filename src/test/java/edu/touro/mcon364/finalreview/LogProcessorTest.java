package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.LogLevel;
import edu.touro.mcon364.finalreview.model.LogMessage;
import edu.touro.mcon364.finalreview.orderflowhandoff.exercises.LogProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LogProcessorTest {

    private LogProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new LogProcessor();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        processor.stop();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private LogMessage msg(LogLevel level) {
        return new LogMessage("src", "text", level, System.currentTimeMillis());
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void totalProcessedIsZeroInitially() {
        assertEquals(0, processor.getTotalProcessed());
    }

    @Test
    void countsByLevelIsEmptyInitially() {
        assertTrue(processor.getCountsByLevel().isEmpty());
    }

    // ── invalid workerCount ───────────────────────────────────────────────────

    @Test
    void startWithZeroWorkersThrows() {
        assertThrows(Exception.class, () -> processor.start(0));
    }

    @Test
    void startWithNegativeWorkersThrows() {
        assertThrows(Exception.class, () -> processor.start(-1));
    }

    // ── single-worker processing ──────────────────────────────────────────────

    @Test
    void singleWorkerProcessesAllMessages() throws InterruptedException {
        processor.start(1);
        for (int i = 0; i < 10; i++) {
            processor.submit(msg(LogLevel.INFO));
        }
        processor.stop();
        assertEquals(10, processor.getTotalProcessed());
    }

    @Test
    void countsAreCorrectForMixedLevels() throws InterruptedException {
        processor.start(1);
        processor.submit(msg(LogLevel.INFO));
        processor.submit(msg(LogLevel.INFO));
        processor.submit(msg(LogLevel.WARN));
        processor.submit(msg(LogLevel.ERROR));
        processor.stop();

        Map<LogLevel, Integer> counts = processor.getCountsByLevel();
        assertEquals(2, counts.get(LogLevel.INFO));
        assertEquals(1, counts.get(LogLevel.WARN));
        assertEquals(1, counts.get(LogLevel.ERROR));
    }

    // ── multiple workers ──────────────────────────────────────────────────────

    @Test
    void multipleWorkersProcessAllMessages() throws InterruptedException {
        processor.start(4);
        for (int i = 0; i < 100; i++) {
            processor.submit(msg(LogLevel.WARN));
        }
        processor.stop();
        assertEquals(100, processor.getTotalProcessed());
    }

    @Test
    void multipleWorkersCountsAreAccurate() throws InterruptedException {
        processor.start(3);
        for (int i = 0; i < 30; i++) {
            processor.submit(msg(LogLevel.ERROR));
        }
        processor.stop();
        assertEquals(30, processor.getCountsByLevel().getOrDefault(LogLevel.ERROR, 0));
    }

    // ── stop waits for remaining work ─────────────────────────────────────────

    @Test
    void stopWaitsForAllSubmittedMessagesToBeProcessed() throws InterruptedException {
        processor.start(2);
        for (int i = 0; i < 50; i++) {
            processor.submit(msg(LogLevel.INFO));
        }
        processor.stop();
        // After stop() returns, all work should be done
        assertEquals(50, processor.getTotalProcessed());
    }

    // ── defensive copy ────────────────────────────────────────────────────────

    @Test
    void getCountsByLevelReturnsCopyNotMutableState() throws InterruptedException {
        processor.start(1);
        processor.submit(msg(LogLevel.INFO));
        processor.stop();

        Map<LogLevel, Integer> counts = processor.getCountsByLevel();
        assertThrows(Exception.class, () -> counts.put(LogLevel.WARN, 99));
    }
}

