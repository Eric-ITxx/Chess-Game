package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class Bishop extends piece {
    //<----Class constructor---->.
     public Bishop(String pos,Color color) {
        super(pos,piecetype.BISHOP,color);
        // give a symbol to the piece according to the color
        if(this.getColor()==Color.WHITE){
            symbol='B';
        }
        else{
            symbol='b';
        }
    }


// Method to get a List of all valid moves of a Bishop
// at a specific position
@Override
public List<String> getPossibleMoves(piece[][] board) {
    // resets all the previous moves Stored in the list.
    moves.clear();
    // to travel in the given directions
    int[][] directions = {
            {-1, -1},// to move upleft
            {-1, 1}, // to move upright
            {1, -1}, // to move downleft
            {1, 1}   // to move downrith
    };
    // find the valid moves in each direction
    for (int[] dir : directions) {
        int r = row + dir[0];
        int c = col + dir[1];

        while (r >= 0 && r < 8 && c >= 0 && c < 8) {
            if (board[r][c] == null) {
                moves.add(rcToAlgebraic(r, c)); // empty square
            } else {
                if (board[r][c].getColor() != this.getColor()) {
                    moves.add(rcToAlgebraic(r, c));
                    break;// capture opponent and stops further in this direction
                }
                break; // stop further in this direction
            }

            r += dir[0]; // to move end of the diagonal
            c += dir[1]; // to move end of the diagonal
        }
    }

    return moves;
}

}
