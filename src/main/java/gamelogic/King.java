package gamelogic;
import java.util.ArrayList;
import java.util.List;

public class King extends piece {
    private boolean hasmoved;
    private List<String> moves=new ArrayList<>();

    //<----Class constructor---->.
    public King( String pos,Color color) {
        super(pos,piecetype.KING,color);
        if(this.getColor()==Color.WHITE){
            symbol='K';
        }
        else{
            symbol='k';
        }
        hasmoved=false;
    }

// at a specific position
    @Override
    public List<String> getPossibleMoves(piece[][] board) {
        // resets all the previous moves Stored in the list.
        moves.clear();
        // to travel in the given directions
        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };

        // Normal king moves (1 square in any direction)
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (board[r][c] == null || board[r][c].getColor() != this.getColor()) {
                    moves.add(rcToAlgebraic(r, c));
                }
            }
        }

        // Castling example (simplified)
        if (!hasmoved) {
            // Kingside castling
            if (board[row][7] != null&&board[row][5] == null && board[row][6] == null &&
                    piecetype.ROOK== board[row][7].getType()) {
                moves.add(rcToAlgebraic(row, 6));
            }

            // Queenside castling
            if (board[row][7] != null&&board[row][1] == null && board[row][2] == null
                    && board[row][3] == null && piecetype.ROOK== board[row][0].getType()) {
                moves.add(rcToAlgebraic(row, 2));
            }
            hasmoved=true;
        }

        return moves;
    }
}

