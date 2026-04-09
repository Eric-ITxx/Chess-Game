package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class Queen extends piece {

    public Queen(String pos,Color color) {
        super(pos,piecetype.QUEEN,color);
        if(this.getColor()==Color.WHITE){
            symbol='Q';
        }
        else{
            symbol='q';
        }
    }


    @Override
    public List<String> getPossibleMoves(piece[][] board) {
        moves.clear();

        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] dir : directions) {
            int r = this.getRow() + dir[0];
            int c = this.getCol() + dir[1];

            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (board[r][c] == null) {
                    moves.add(rcToAlgebraic(r, c)); // empty square
                } else {
                    if (board[r][c].getColor() != this.getColor() ) {
                        moves.add(rcToAlgebraic(r, c)); // capture
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
