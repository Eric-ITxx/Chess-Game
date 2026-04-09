package gamelogic;

import java.util.ArrayList;
import java.util.List;

import static gamelogic.Board.isCheckmate;

public abstract class  piece{
     protected int row;
     protected int col;
     protected piecetype type;
     protected List<String> moves;
     protected Color color;
     protected char symbol;
     protected String pos;


     public boolean move(String sq) {
             // call to get all possible moves at this specified position
             getPossibleMoves(Board.getBoard());
             // Checks whether the given move is valid or not
             for (String move : moves) {
                 if (sq.equals(move)) {

                     if(Board.isMoveLegal(this, move) ){
                         Board.getBoard()[this.getRow()][this.getCol()] = null;
                         int[] rc = algebraicToRC(sq);
                         row = rc[0];
                         col = rc[1];
                         Board.getBoard()[row][col] = this;
                         setPosition(rcToAlgebraic(row,col));
                         //Prints a msg to show a check to king
                         if(checker()){
                             System.out.println("Check to KING by " + getColor() +
                                     " BISHOP from position " + rcToAlgebraic(row, col));
                         }
                         return true;
                     }
                 }
             }
             return false;
         }

     public piece(String pos,piecetype type,Color color){
         this.pos=pos;
         this.symbol='-';
         this.type=type;
         this.color=color;
         this.moves=new ArrayList<>();
         int[] rc=algebraicToRC(pos);
         this.row=rc[0];
         this.col=rc[1];
         setPosition(pos);
     }
     public  int getRow(){
         return  this.row;
     }
     public  int getCol(){
         return  this.col;
     }
     public Color getColor(){
         return this.color;
     }
     public piecetype getType(){
         return this.type;
     }
     public String getPos(){
         return this.pos;
     }
     public char getSymbol(){
         return this.symbol;
     }

     public static int[] algebraicToRC(String alg) {
         char file = alg.charAt(0); // 'a'..'h'
         char rank = alg.charAt(1); // '1'..'8'
         int col = file - 'a';
         int rankNum = rank-'0' ; // 1..8

         return new int[] { rankNum,col };
     }
     public static boolean inBounds(int row, int col) {

         return row >= 0 && row < 8 && col >= 0 && col < 8;
     }

     // Move the piece to the specific position by
     // considering whether it is valid or leaving any check


     public static String rcToAlgebraic(int row, int col) {
         char file = (char) ('a' + col);
         int rank = row;
         return "" + file + rank;
     }
     public abstract List<String> getPossibleMoves(piece[][] board);
     // Sets the row and column to the new position of the bishop
     // Sets the row and column to the new position of the piece
     public  void setPosition(String sq ){
         int[]rc= algebraicToRC(sq);
         Board.getBoard()[rc[0]][rc[1]] = this;
     }


     // Checks whether it is giving check to opposite king piece
     public boolean checker() {
         getPossibleMoves(Board.getBoard());
         for(String move : moves){
             int[] rc = algebraicToRC(move);
             if(Board.getBoard()[rc[0]][rc[1]]!=null){
                 if(Board.getBoard()[rc[0]][rc[1]].getType()==piecetype.KING &&
                         Board.getBoard()[rc[0]][rc[1]].getColor()!=this.getColor()){

                     return true;
                 }
             }
         }
         return false;
     }

     public static boolean isValidSquare(String alg) {
         if (alg == null || alg.length() != 2)
             return false;
         char file = alg.charAt(0);
         char rank = alg.charAt(1);
         if(file >= 'a' && file <= 'h' && rank >= '1' && rank <= '8')
             return true;
         else
             return false;
     }

}


