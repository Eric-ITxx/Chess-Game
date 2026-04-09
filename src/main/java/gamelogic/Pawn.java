package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends piece {

    public Pawn( String pos, Color color) {
        super(pos, piecetype.PAWN, color);
        if (this.getColor() == Color.WHITE) {
            symbol = 'P';
        }
        else {
            symbol = 'p';
        }
    }


    @Override
    public List<String> getPossibleMoves(piece[][] board) {
        moves.clear();
        for(int m=0;m<Board.getBoard().length;m++){
            for(int n=0;n<Board.getBoard().length;n++){
                if(Board.getBoard()[m][n]==this){
                    int direction = (this.getColor() == Color.WHITE) ? -1 : 1;
                    int startRow = (this.getColor() == Color.WHITE )? 6 : 1;

                    // Forward one square
                    int forwardRow = this.getRow() + direction;
                    if (forwardRow >= 0 && forwardRow < 8 && board[forwardRow][this.getCol()] == null  ) {
                        moves.add(rcToAlgebraic(forwardRow, this.getCol()));

                        // Move two squares from starting position
                        int doubleRow = this.getRow() + 2 * direction;
                        if (this.getRow() == startRow && board[doubleRow][this.getCol()] == null)
                            moves.add(rcToAlgebraic(doubleRow, this.getCol()));
                    }

                    // Capture diagonally
                    int[][] captures = {{direction, -1}, {direction, 1}};
                    for (int[] cap : captures) {
                        int r = this.getRow() + cap[0];
                        int c = this.getCol() + cap[1];
                        if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                            if (board[r][c] != null && board[r][c].getColor() != this.getColor())
                                moves.add(rcToAlgebraic(r, c));
                        }
                    }
                }
            }
        }
        return moves;
    }
}

