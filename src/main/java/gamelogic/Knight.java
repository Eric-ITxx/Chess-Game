package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class Knight extends piece {

    //<----Class constructor---->.
    public Knight(String sq, Color color) {
        super(sq, piecetype.KNIGHT, color);

        if (this.getColor() == Color.WHITE) {
            symbol = 'H';
        }
        else {
            symbol = 'h';
        }
    }


    @Override
    public List<String> getPossibleMoves(piece[][] board) {
        // resets all the previous moves Stored in the list.
        moves.clear();
        // directions to move
        int[][] jumps = {
                {-2, -1},
                {-2, 1},
                {-1, -2},
                {-1, 2},
                {1, -2},
                {1, 2},
                {2, -1},
                {2, 1}
        };
        // find the valid moves in each direction
        for (int[] j : jumps) {
            int r = row + j[0];
            int c = col + j[1];

            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (board[r][c] == null || board[r][c].getColor() != this.getColor()) {
                    moves.add(rcToAlgebraic(r, c));
                }
            }
        }

        return moves;
    }
}

