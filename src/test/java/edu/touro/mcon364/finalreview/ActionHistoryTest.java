package edu.touro.mcon364.finalreview;

import edu.touro.mcon364.finalreview.model.Action;
import edu.touro.mcon364.finalreview.orderflowhandoff.exercises.ActionHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActionHistoryTest {

    private ActionHistory history;

    @BeforeEach
    void setUp() {
        history = new ActionHistory();
    }

    // ── initial state ──────────────────────────────────────────────────────────

    @Test
    void undoCountIsZeroInitially() {
        assertEquals(0, history.getUndoCount());
    }

    @Test
    void redoCountIsZeroInitially() {
        assertEquals(0, history.getRedoCount());
    }

    @Test
    void undoReturnsEmptyWhenNothingPerformed() {
        assertEquals(Optional.empty(), history.undo());
    }

    @Test
    void redoReturnsEmptyWhenNothingPerformed() {
        assertEquals(Optional.empty(), history.redo());
    }

    // ── perform ───────────────────────────────────────────────────────────────

    @Test
    void performIncreasesUndoCount() {
        history.perform(new Action("type A", 1));
        assertEquals(1, history.getUndoCount());
    }

    @Test
    void performMultipleActionsIncreasesUndoCount() {
        history.perform(new Action("type A", 1));
        history.perform(new Action("type B", 2));
        history.perform(new Action("type C", 3));
        assertEquals(3, history.getUndoCount());
    }

    @Test
    void performDoesNotIncreaseRedoCount() {
        history.perform(new Action("type A", 1));
        assertEquals(0, history.getRedoCount());
    }

    // ── undo ──────────────────────────────────────────────────────────────────

    @Test
    void undoReturnsLastPerformedAction() {
        Action a = new Action("type A", 1);
        history.perform(a);
        assertEquals(Optional.of(a), history.undo());
    }

    @Test
    void undoReturnsActionsInLifoOrder() {
        Action a = new Action("first", 1);
        Action b = new Action("second", 2);
        history.perform(a);
        history.perform(b);
        assertEquals(Optional.of(b), history.undo());
        assertEquals(Optional.of(a), history.undo());
    }

    @Test
    void undoDecrementsUndoCount() {
        history.perform(new Action("type A", 1));
        history.perform(new Action("type B", 2));
        history.undo();
        assertEquals(1, history.getUndoCount());
    }

    @Test
    void undoIncrementsRedoCount() {
        history.perform(new Action("type A", 1));
        history.undo();
        assertEquals(1, history.getRedoCount());
    }

    @Test
    void undoReturnsEmptyAfterAllActionsUndone() {
        history.perform(new Action("type A", 1));
        history.undo();
        assertEquals(Optional.empty(), history.undo());
    }

    // ── redo ──────────────────────────────────────────────────────────────────

    @Test
    void redoReturnsUndoneAction() {
        Action a = new Action("type A", 1);
        history.perform(a);
        history.undo();
        assertEquals(Optional.of(a), history.redo());
    }

    @Test
    void redoRestoresActionsInFifoOrder() {
        Action a = new Action("first", 1);
        Action b = new Action("second", 2);
        history.perform(a);
        history.perform(b);
        history.undo(); // undo b
        history.undo(); // undo a
        assertEquals(Optional.of(a), history.redo());
        assertEquals(Optional.of(b), history.redo());
    }

    @Test
    void redoDecrementsRedoCount() {
        history.perform(new Action("type A", 1));
        history.undo();
        history.redo();
        assertEquals(0, history.getRedoCount());
    }

    @Test
    void redoIncrementsUndoCount() {
        history.perform(new Action("type A", 1));
        history.undo();
        history.redo();
        assertEquals(1, history.getUndoCount());
    }

    @Test
    void redoReturnsEmptyWhenNoRedoAvailable() {
        history.perform(new Action("type A", 1));
        assertEquals(Optional.empty(), history.redo());
    }

    // ── perform clears redo history ────────────────────────────────────────────

    @Test
    void performAfterUndoClearsRedoHistory() {
        history.perform(new Action("first", 1));
        history.perform(new Action("second", 2));
        history.undo();
        history.perform(new Action("new action", 3));
        assertEquals(0, history.getRedoCount());
        assertEquals(Optional.empty(), history.redo());
    }

    @Test
    void performAfterUndoRedoPathIsInvalidated() {
        Action a = new Action("first", 1);
        Action b = new Action("second", 2);
        Action c = new Action("third", 3);
        history.perform(a);
        history.perform(b);
        history.undo(); // undo b, b is on redo stack
        history.perform(c); // new action should clear b from redo
        assertEquals(0, history.getRedoCount());
        assertEquals(2, history.getUndoCount()); // a and c
    }
}

