package gamelogic;

public class Player {

    private Color color;
    private String playername;
    private piece[] pieces;


    private int rating;
    private int wins;
    private int losses;

    // ---------------- CONSTRUCTOR ----------------
    public Player(Color color, String playername) {
        this.color = color;
        this.playername = playername;


        this.rating = 1000;
        this.wins = 0;
        this.losses = 0;

        pieces = new piece[16];
        if (this.color == Color.WHITE) {
            setinitialpositionsofwhite();
        } else {
            setinitialpositionsofblack();
        }
    }

    // ---------------- GETTERS ----------------
    public String getPlayername() {
        return playername;
    }

    public Color getColor() {
        return color;
    }

    public piece[] getPieces() {
        return pieces;
    }

    public int getRating() {
        return rating;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    // ---------------- GAME RESULT METHODS ----------------


    public void winGame() {
        wins++;
        rating += 20;   // simple increase
    }


    public void loseGame() {
        losses++;
        rating -= 20;   // simple decrease

        if (rating < 0) {
            rating = 0; // safety
        }
    }

    // ---------------- INITIAL POSITIONS ----------------
    public void setinitialpositionsofwhite() {
        pieces[0] = new Pawn("a6", Color.WHITE);
        pieces[1] = new Pawn("b6", Color.WHITE);
        pieces[2] = new Pawn("c6", Color.WHITE);
        pieces[3] = new Pawn("d6", Color.WHITE);
        pieces[4] = new Pawn("e6", Color.WHITE);
        pieces[5] = new Pawn("f6", Color.WHITE);
        pieces[6] = new Pawn("g6", Color.WHITE);
        pieces[7] = new Pawn("h6", Color.WHITE);
        pieces[8] = new Bishop("c7", Color.WHITE);
        pieces[9] = new Bishop("f7", Color.WHITE);
        pieces[10] = new Rook("a7", Color.WHITE);
        pieces[11] = new Rook("h7", Color.WHITE);
        pieces[12] = new Knight("b7", Color.WHITE);
        pieces[13] = new Knight("g7", Color.WHITE);
        pieces[14] = new King("e7", Color.WHITE);
        pieces[15] = new Queen("d7", Color.WHITE);
    }

    public void setinitialpositionsofblack() {
        pieces[0] = new Pawn("a1", Color.BLACK);
        pieces[1] = new Pawn("b1", Color.BLACK);
        pieces[2] = new Pawn("c1", Color.BLACK);
        pieces[3] = new Pawn("d1", Color.BLACK);
        pieces[4] = new Pawn("e1", Color.BLACK);
        pieces[5] = new Pawn("f1", Color.BLACK);
        pieces[6] = new Pawn("g1", Color.BLACK);
        pieces[7] = new Pawn("h1", Color.BLACK);
        pieces[8] = new Bishop("c0", Color.BLACK);
        pieces[9] = new Bishop("f0", Color.BLACK);
        pieces[10] = new Rook("a0", Color.BLACK);
        pieces[11] = new Rook("h0", Color.BLACK);
        pieces[12] = new Knight("b0", Color.BLACK);
        pieces[13] = new Knight("g0", Color.BLACK);
        pieces[14] = new King("e0", Color.BLACK);
        pieces[15] = new Queen("d0", Color.BLACK);
    }
}
