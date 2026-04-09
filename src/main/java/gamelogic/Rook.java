package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class Rook extends piece {
    public Rook(String sq,Color color) {
        super(sq,piecetype.ROOK,color);

        if(this.getColor()==Color.WHITE){
            symbol='R';
        }
        else{
            symbol='r';
        }
    }



    @Override
    public List<String> getPossibleMoves(piece[][] board) {
        moves.clear();

        int[][] directions = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
        };

        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];

            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (board[r][c] == null) {
                    moves.add(rcToAlgebraic(r, c)); // empty square
                } else {
                    if (board[r][c].getColor() != this.getColor()) {
                        moves.add(rcToAlgebraic(r, c)); // capture opponent
                    }
                    break; // stop after hitting any piece
                }

                r += dir[0];
                c += dir[1];
            }
        }

        return moves;
    }
}

