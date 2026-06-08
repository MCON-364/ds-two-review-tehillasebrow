package edu.touro.mcon364.finalreview.orderflowhandoff.exercises;

import edu.touro.mcon364.finalreview.model.Action;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * In-class Exercise 1 — Action History
 *
 * A simple editor needs to remember actions so the user can undo and redo work.
 *
 * Requirements:
 * - perform(action) records a newly completed action.
 * - undo() removes and returns the action that should be undone next.
 * - redo() removes and returns the action that should be redone next.
 * - undo() returns Optional.empty() when there is nothing available to undo.
 * - redo() returns Optional.empty() when there is nothing available to redo.
 * - performing a new action after one or more undo operations makes the old redo path invalid.
 * - getUndoCount() returns how many actions are currently available to undo.
 * - getRedoCount() returns how many actions are currently available to redo.
 *
 * You may add private fields and private helper methods.
 * Do not change the public method signatures.
 * Before coding, decide:
 * - What information does this class need to remember?
 * - What is the appropriate data structure
 * - Which operation should be fastest?
 * - When an action is undone, where should it go so it can be redone later?
 * - What should happen to redo history after a brand-new action is performed?

 */
public class ActionHistory {
    // THE BIG IDEA: undo/redo is TWO STACKS. A stack is "Last In, First Out" (LIFO),
    // like a stack of plates — the last plate you put on is the first you take off.
    // undoStack: actions you could undo (most recent on top).
    // redoStack: actions you undid and could redo.
    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();

    public void perform(Action action) {
        // push = put the new action on TOP of the undo stack (it's the newest thing to undo).
        undoStack.push(action);
        // A brand-new action invalidates the redo path, so wipe the redo stack.
        // (Once you do something new, "redo" of the old branch no longer makes sense.)
        redoStack.clear();
    }

    public Optional<Action> undo() {
        // Nothing to undo? Hand back an empty box.
        if (undoStack.isEmpty())
            return Optional.empty();
        else
            // Take the top action off undo (remove() removes the top), and push it
            // onto redo so it can be redone later.
            redoStack.push(undoStack.remove());
        // Return the action we just moved (now on top of redo) wrapped in an Optional.
        return Optional.ofNullable(redoStack.peek());
    }

    public Optional<Action> redo() {
        // Nothing to redo? Empty box.
        if (redoStack.isEmpty())
            return Optional.empty();
        else
            // Move the top action from redo back onto undo (we're re-doing it).
            undoStack.push(redoStack.remove());
        // Return the action we just moved (now on top of undo).
        return Optional.ofNullable(undoStack.peek());
    }

    public int getUndoCount() {
        // How many actions are currently available to undo.
        return undoStack.size();
    }

    public int getRedoCount() {
        // How many actions are currently available to redo.
        return redoStack.size();
    }
}
