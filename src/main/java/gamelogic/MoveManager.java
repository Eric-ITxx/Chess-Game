package gamelogic;

import java.util.Stack;

public class MoveManager {

    private final Stack<Snapshot> undoStack;
    private final Stack<Snapshot> redoStack;

    public  MoveManager(){
        undoStack = new Stack<>();
        redoStack = new Stack<>();
    }
    // Call this once at game start (initial position)
    public void recordInitial(Snapshot s) {
        undoStack.clear();
        redoStack.clear();
        undoStack.push(s);
    }

    // Call this AFTER you apply a move successfully
    public void recordAfterMove(Snapshot s) {
        undoStack.push(s);
        redoStack.clear(); // important!
    }

    public boolean canUndo() {
        return undoStack.size() > 1; // at least 2 states needed (current + previous)
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public Snapshot undo(Snapshot current) {
        if (!canUndo())
            return current;

        redoStack.push(current);     // save current for redo
        undoStack.pop();             // remove current from undo stack
        return undoStack.peek();     // previous becomes current
    }

    public Snapshot redo(Snapshot current) {
        if (!canRedo()) return current;

        Snapshot next = redoStack.pop();
        undoStack.push(next);
        return next;
    }
    public Stack<Snapshot> getUndoStack() {
        return undoStack;
    }
    public Stack<Snapshot> getRedoStack() {
        return redoStack;
    }
}

