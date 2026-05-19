package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.SensorReading;
import edu.touro.mcon364.finalreview.orderflowhandoff.homework.SensorProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.*;

class SensorProcessorTest {

    private SensorProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new SensorProcessor();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        processor.stop();
    }

    private SensorReading reading(double value) {
        return new SensorReading("sensor-1", value, System.nanoTime());
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    void totalProcessedIsZeroInitially() {
        assertEquals(0, processor.getTotalProcessed());
    }

    @Test
    void statsAreEmptyInitially() {
        DoubleSummaryStatistics stats = processor.getStats();
        assertEquals(0L, stats.getCount());
    }

    // ── invalid workerCount ───────────────────────────────────────────────────

    @Test
    void startWithZeroWorkersThrows() {
        assertThrows(IllegalArgumentException.class, () -> processor.start(0));
    }

    @Test
    void startWithNegativeWorkersThrows() {
        assertThrows(IllegalArgumentException.class, () -> processor.start(-3));
    }

    // ── basic processing ─────────────────────────────────────────────────────

    @Test
    void singleWorkerProcessesAllReadings() throws InterruptedException {
        processor.start(1);
        for (int i = 0; i < 20; i++) {
            processor.submit(reading(i));
        }
        processor.stop();
        assertEquals(20, processor.getTotalProcessed());
    }

    @Test
    void multipleWorkersProcessAllReadings() throws InterruptedException {
        processor.start(4);
        for (int i = 0; i < 100; i++) {
            processor.submit(reading(i));
        }
        processor.stop();
        assertEquals(100, processor.getTotalProcessed());
    }

    // ── stats accuracy ────────────────────────────────────────────────────────

    @Test
    void statsCountMatchesTotalProcessed() throws InterruptedException {
        processor.start(2);
        for (int i = 0; i < 30; i++) {
            processor.submit(reading(i + 1));
        }
        processor.stop();
        assertEquals(processor.getTotalProcessed(), (int) processor.getStats().getCount());
    }

    @Test
    void statsMinIsCorrect() throws InterruptedException {
        processor.start(1);
        processor.submit(reading(5.0));
        processor.submit(reading(10.0));
        processor.submit(reading(1.0));
        processor.stop();
        assertEquals(1.0, processor.getStats().getMin(), 0.001);
    }

    @Test
    void statsMaxIsCorrect() throws InterruptedException {
        processor.start(1);
        processor.submit(reading(5.0));
        processor.submit(reading(10.0));
        processor.submit(reading(1.0));
        processor.stop();
        assertEquals(10.0, processor.getStats().getMax(), 0.001);
    }

    @Test
    void statsSumIsCorrect() throws InterruptedException {
        processor.start(1);
        processor.submit(reading(3.0));
        processor.submit(reading(7.0));
        processor.stop();
        assertEquals(10.0, processor.getStats().getSum(), 0.001);
    }

    @Test
    void statsAverageIsCorrect() throws InterruptedException {
        processor.start(1);
        processor.submit(reading(4.0));
        processor.submit(reading(8.0));
        processor.stop();
        assertEquals(6.0, processor.getStats().getAverage(), 0.001);
    }

    // ── stop waits for all work ───────────────────────────────────────────────

    @Test
    void stopWaitsForAllSubmittedReadingsToBeProcessed() throws InterruptedException {
        processor.start(2);
        for (int i = 0; i < 50; i++) {
            processor.submit(reading(i));
        }
        processor.stop();
        assertEquals(50, processor.getTotalProcessed());
    }

    // ── thread safety (concurrent submissions) ────────────────────────────────

    @Test
    void concurrentSubmissionsAreAllProcessed() throws InterruptedException {
        processor.start(3);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50; i++) processor.submit(reading(1.0));
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) processor.submit(reading(2.0));
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        processor.stop();
        assertEquals(100, processor.getTotalProcessed());
    }
}

