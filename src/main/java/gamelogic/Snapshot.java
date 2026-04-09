package gamelogic;
import java.lang.String;
import java.io.*;

import java.util.List;

public class Snapshot  {

    private  StringBuilder code=new StringBuilder();
    private String   boardstring;
    public String   getBoardstring() {
        return boardstring;
    }
    private static Color color;
    // instance color for undo/redo (does not break existing code)
    private Color snapshotColor;
    public static Color getColor() {
        return color;
    }

    public Color getSnapshotColor() {
        return snapshotColor;
    }
    public StringBuilder getCode() {
        return code;
    }
    public Snapshot(Color color) throws IOException {
        boardstring=encoding(Board.getBoard());
        this.color = color;
        this.snapshotColor = color;
    }
    public  String encoding(piece[][] pix) throws IOException{
        BufferedWriter bw=new BufferedWriter(
                new FileWriter(new File("String.txt")));
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                if(Board.getBoard()[i][j]==null){
                    code.append('-');
                    bw.write('-');
                }else{
                    piece pi=Board.getBoard()[i][j];
                    char p=piecetochar(pi);
                    bw.write(p);
                    code.append(p);
                }
            }
        }
        bw.close();
        return code.toString();
    }

    public  char piecetochar(piece p) {
        char ch=  switch (p.getType()) {
            case PAWN -> 'P';
            case KING -> 'K';
            case QUEEN -> 'Q';
            case BISHOP -> 'B';
            case ROOK -> 'R';
            case KNIGHT -> 'N';
        };
        return (p.getColor()==Color.WHITE)?ch:Character.toLowerCase(ch);
    }

    public  static piece[][] decode() throws IOException {
        BufferedReader br=new BufferedReader(new FileReader("String.txt"));
        piece[][] board = new piece[8][8];

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char ch = (char)br.read();
                board[r][c] = (ch == '-') ? null : charToPiece(ch,piece.rcToAlgebraic(r,c));
            }

        }
        return board;
    }

    public static piece[][] decode(String s) throws IOException {
        String[] parts = new String[8];
        for (int i = 0; i < 8; i++) {
            parts[i] = s.substring(i * 8, (i + 1) * 8);
        }
        piece[][] board = new piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                char ch = parts[r].charAt(c);
                board[r][c] = (ch == '-') ? null : charToPiece(ch, piece.rcToAlgebraic(r, c));
            }
        }
        return board;
    }

    private static piece charToPiece(char ch,String pos) {
        Color color = Character.isUpperCase(ch) ? Color.WHITE : Color.BLACK;
        char up = Character.toUpperCase(ch);

        piece type = switch (up) {
            case 'P' -> new Pawn(pos, color);
            case 'N' -> new Knight(pos, color);
            case 'B' -> new Bishop(pos, color);
            case 'R' -> new Rook(pos, color);
            case 'Q' -> new Queen(pos, color);
            case 'K' -> new King(pos, color);
            default -> throw new IllegalArgumentException("Illegal character " + ch);
        };
        return type;
    }

    public  void savesnapshopt(piece[][] pix,Color color) throws IOException {
        encoding(pix);
        this.color=color;
        this.snapshotColor = color;
    }

    public static piece[][] loadsnapshot() throws IOException {
        return decode() ;
    }

}
