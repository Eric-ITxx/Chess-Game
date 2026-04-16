package gamelogic;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
public class Board {

    public static final int SIZE = 8;// finalizes the size of Board
    //Creates a Board that is shared by all the objects.(it adds all the pieces to a single Board)
    private static final piece[][] board=new piece[SIZE][SIZE];
    // method to get the Board.
    public static piece[][] getBoard(){
        return board;
    }

    // Constructor that initializes every box of board to null
    public Board() {
        for(int i=0;i<SIZE;i++){
            for(int j=0;j<SIZE;j++){
                Board.getBoard()[i][j]=null;
            }
        }
    }
    // overloaded version of class constructor

    //Method to diplay the board in console
    // Symbol (-) for null Capital symbol for white small symbol for white
    public static void displayBoard(){
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if(Board.getBoard()[r][c]==null){
                    System.out.print("- ");
                }else{
                    System.out.print(Board.getBoard()[r][c].getSymbol()+" ");
                }

            }
            System.out.println();
        }
    }
    // Checks whether a move is not leaving any check behind to its own king
    public static boolean isMoveLegal(piece p, String targetSquare) {
        // Make a DEEP copy of the board
        piece[][] tempBoard = new piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                tempBoard[r][c] = board[r][c];
            }
        }
        // Move the piece in deep copy board and checks that by doing it
        // whether the king is not in the check
        int[] rc = p.algebraicToRC(targetSquare);
        int newRow = rc[0];
        int newCol = rc[1];

        int oldRow = p.getRow();
        int oldCol = p.getCol();

        // Simulate the move on the copied board
        tempBoard[newRow][newCol] = p;
        tempBoard[oldRow][oldCol] = null;


        // Check if king is safe on the simulated board
        boolean safe = !isKingInCheck(p.getColor(), tempBoard);
        return safe;
    }
    //  Check if king is safe on the simulated board
//  Check if king is safe on the simulated board
    public static boolean isKingInCheck(Color color, piece[][] board) {
        piece king = null;
        int kingRow = -1, kingCol = -1;

        // Find the King of this color
        outer:
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                piece p = board[r][c];
                if (p != null && p.getType() == piecetype.KING && p.getColor() == color) {
                    king = p;
                    kingRow = r;
                    kingCol = c;
                    break ; // stop searching once found
                }

            }
        }



        String kingPos = king.rcToAlgebraic(kingRow, kingCol);

        // Check if any opponent piece can attack the king
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                piece enemy = board[r][c];
                if (enemy != null && enemy.getColor() != color) {
                    List<String> enemyMoves = enemy.getPossibleMoves(board);
                    if (enemyMoves.contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Checks whether any move of any piece can remove the check of King.
    public static boolean isCheckmate(Color color) {
        piece[][] currentBoard = Board.getBoard();
        for(int i=0;i<SIZE;i++){
            for(int j=0;j<SIZE;j++){
                currentBoard[i][j] = board[i][j];
            }
        }

        // If king is not in check, it's not checkmate
        if (!isKingInCheck(color, currentBoard)) {
            return false;
        }
        piece[][] tempBoard = new piece[SIZE][SIZE];
        // Loop through all pieces of this color
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                tempBoard[r][c] = currentBoard[r][c];
                if ( tempBoard[r][c] != null && tempBoard[r][c].getColor() == color) {
                    List<String> possibleMoves = tempBoard[r][c].getPossibleMoves(currentBoard);
                    for (String move : possibleMoves) {
                        //  Simulate the move on the copied board
                        int oldRow = tempBoard[r][c].getRow();
                        int oldCol = tempBoard[r][c].getCol();
                        int[] newRC = tempBoard[r][c].algebraicToRC(move);
                        int newRow = newRC[0];
                        int newCol = newRC[1];

                        piece movingPiece = tempBoard[oldRow][oldCol];
                        tempBoard[oldRow][oldCol] = null;
                        tempBoard[newRow][newCol] = movingPiece;

                        // Temporarily update the piece's position
                        movingPiece.setPosition(movingPiece.rcToAlgebraic(newRow, newCol));

                        //  Check if this simulated move gets king out of check
                        boolean stillInCheck = isKingInCheck(color, tempBoard);

                        // Restore position in case we reuse same piece object
                        movingPiece.setPosition(movingPiece.rcToAlgebraic(oldRow, oldCol));

                        // If king is no longer in check after this move → not checkmate
                        if (!stillInCheck) {
                            return false;
                        }
                    }
                }
            }
        }

        //  No move removes check , it's checkmate
        return true;
    }
    // -----------------------------------
    /** Returns true when the given color has no legal moves AND is NOT in check (stalemate). */
    public static boolean isStalemate(Color color) {
        if (isKingInCheck(color, board)) return false; // in check = not stalemate
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                piece p = board[r][c];
                if (p != null && p.getColor() == color) {
                    List<String> moves = p.getPossibleMoves(board);
                    for (String mv : moves) {
                        if (isMoveLegal(p, mv)) return false;
                    }
                }
            }
        }
        return true;
    }

    /** Replace the pawn at (row,col) with the chosen promotion piece in-place. */
    public static void promotePawn(int row, int col, piecetype newType) {
        piece pawn = board[row][col];
        if (pawn == null || pawn.getType() != piecetype.PAWN) return;
        Color color = pawn.getColor();
        String pos = piece.rcToAlgebraic(row, col);
        piece promoted = switch (newType) {
            case ROOK   -> new Rook(pos, color);
            case BISHOP -> new Bishop(pos, color);
            case KNIGHT -> new Knight(pos, color);
            default     -> new Queen(pos, color);
        };
        board[row][col] = promoted;
    }

    // -----------------------------------
    public static List<piece> getPieces(Color color) {
        List<piece> pieces = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                piece p = board[r][c];
                if (p != null && p.getColor() == color) {
                    pieces.add(p);
                }
            }
        }
        return pieces;
    }

    public static void displayresumeboard(piece[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if(board[r][c] != null) {
                    System.out.println(board[r][c].getSymbol()+" ");
                }
                else  {
                    System.out.println("- ");
                }

            }
            System.out.println();
        }
    }

    public static piece[][] loadsnapshot() throws IOException {
       return Snapshot.loadsnapshot();
    }
     public static void displaysboard() throws IOException {
        piece[][] tempBoard = loadsnapshot();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = tempBoard[r][c];
                if(board[r][c] == null) {
                    System.out.print("- ");
                }else {
                    System.out.print(board[r][c].getSymbol() + " ");
                }
            }
            System.out.println();
        }
     }

     public static piece findpiecebypos(String Pos){
        int[] rc= piece.algebraicToRC(Pos);
        return  board[rc[0]][rc[1]];
     }

     public static void displayundoredo(String s) throws IOException {
        piece[][] tempboard= Snapshot.decode(s);
         for (int r = 0; r < SIZE; r++) {
             for (int c = 0; c < SIZE; c++) {
                 board[r][c] = tempboard[r][c];
                 if(board[r][c] == null) {
                     System.out.print("- ");
                 }else {
                     System.out.print(board[r][c].getSymbol() + " ");
                 }
             }
             System.out.println();
         }
     }




}


