package makingstats;
import java.util.ArrayList;
import java.util.List;

public class History {
    // Creates a list to Store all the moves of the game
    private List<String> moves;

   // Constructor of the class that creates a list of moves.
    public History() {
        moves = new ArrayList<>();
    }
   // add moves to the list
    public void addMove(String name, String from, String to) {
        String moveRecord = name + " moves from " + from + " to " + to;
        moves.add(moveRecord);
    }

 // Method to print all the moves in the record
    public void printHistory() {
        System.out.println("=== Move History ===");
        int moveNumber = 1;
        for (String move : moves) {
            System.out.println(moveNumber + ". " + move);
            moveNumber++;
        }
    }
    // Method to get the move List.
    public List<String> getMoves() {
        return moves;
    }
    // Clears the move list
    public void clearHistory() {
        moves.clear();
    }

}
