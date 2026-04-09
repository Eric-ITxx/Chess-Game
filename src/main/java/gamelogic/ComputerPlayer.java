package gamelogic;
import Network.Move;
import java.util.List;
import java.util.Random;

public class ComputerPlayer {
    private Random rand = new Random();

    public Move getNextMove(Board board, Color color) {
        List<piece> pieces = board.getPieces(color);
        List<Move> moves = new java.util.ArrayList<>();

        for (piece p : pieces) {
            for (String sq : p.getPossibleMoves(Board.getBoard())) {
                if (Board.isMoveLegal(p, sq)) {
                    int[] rc = p.algebraicToRC(sq);
                    moves.add(new Move(p.getRow(), p.getCol(), rc[0], rc[1]));
                }
            }
        }

        if (moves.isEmpty()) return null;
        return moves.get(rand.nextInt(moves.size()));
    }
}
