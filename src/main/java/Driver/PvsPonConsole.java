// import class Scanner to take input from the user
package Driver;
import Network.Move;
import gamelogic.*;
import makingstats.*;
import Frontend.*;

import java.io.IOException;
import java.util.Scanner;
public class PvsPonConsole {
    public static void main(String[] args) throws IOException {
        boolean win = false;
        // Creates a obj of board
        Board board = new Board();
        // make a array of two players that play the game
        Player[] pl = new Player[2];
        MoveManager m = new MoveManager();
//        m.recordInitial(new Snapshot(Color.WHITE));


        //Makes objects of playerss
        pl[0] = new Player(Color.WHITE, "Ali");
        pl[1] = new Player(Color.BLACK, "Dani");

        // Creates a object of Players to add current two players to
        // the list of Players that have played the Chess
        Players p1 = new Players();
        // add both players to list of players
        Players.addPlayer(pl[1].getPlayername(), 800);
        Players.addPlayer(pl[0].getPlayername(), 600);

        //Creates a object of Class turn that controls the turns of players
        Playerturn turn = new Playerturn(Color.WHITE);
        Scanner sc = new Scanner(System.in);
        int piecenum = 0;
        String moveto;
        Snapshot snapshot = new Snapshot(Color.WHITE);
        // Creates a obj of record that records all the moves of game
        History record = new History();

        System.out.println("Enter 1 or 2 from menu");
        System.out.println("Do you want to play the new game,Press 1");
        System.out.println("Do you want to play the saved gamed,Press 2");
        System.out.print("Please enter your choice:");
        switch (sc.nextInt()) {
            case 1 -> {
                Board.displayBoard();
                m.recordInitial(new Snapshot(turn.getCurrentTurn()));
            }
            case 2 -> {
                Board.displaysboard();
                m.recordInitial(new Snapshot(turn.getCurrentTurn()));
            }
            default -> System.out.println("Invalid input");
        }


        // displays the board in console in form of symbols
        //Loop to move the total number of moves
        System.out.println("Welcome to the chess board PvsP on console");
        System.out.println();
        System.out.println();
        do {

            //Checks whether it is white turn or Black turn
            if (turn.getCurrentTurn() == Color.WHITE) {
                if (Board.isCheckmate(Color.WHITE)) {
                    System.out.println("Checkmate, " + pl[1].getPlayername() + "wins the game ");
                    pl[1].winGame();
                    win = true;
                }
                System.out.println(turn.getCurrentTurn() + "'s turn has Started");
                // Takes input from user to move a specific piece
                if (Board.findpiecebypos(sc.next()).move(moveto = sc.next())) {
                    // add this move to record of the game
                    record.addMove(pl[0].getPlayername(), pl[0].getPieces()[piecenum].getPos(), moveto);
                    turn.endTurn();
                    m.recordAfterMove(new Snapshot(turn.getCurrentTurn()));
                    snapshot.savesnapshopt(Board.getBoard(), turn.getCurrentTurn());
                } else {
                    System.out.println("Invalid move, please try again");
                    if (Board.findpiecebypos(sc.next()).move(moveto = sc.next())) {
                        record.addMove(pl[0].getPlayername(), pl[0].getPieces()[piecenum].getPos(), moveto);
                        turn.endTurn();
                        m.recordAfterMove(new Snapshot(turn.getCurrentTurn()));
                        snapshot.savesnapshopt(Board.getBoard(), turn.getCurrentTurn());
                    }
                }

                System.out.println();
                System.out.println();
                board.displayBoard();
                System.out.println();
                System.out.println();
                if(m.canUndo()){
                    System.out.println("Press 1 to undo the move");
                    switch (sc.nextInt()) {
                        case 1 -> {
                            m.undo(new Snapshot(turn.getCurrentTurn()));
                            Board.displayundoredo(m.getUndoStack().peek().getBoardstring());
                            if(!m.canUndo()){
                                System.out.println("undo cannot be done at this stage ");
                                System.out.println("Press 1 to redo the move");
                                if(sc.nextInt() == 1) {
                                    m.redo(new Snapshot(turn.getCurrentTurn()));
                                    Board.displayundoredo(m.getUndoStack().peek().getBoardstring());
                                }
                            } else if (m.canRedo()) {
                                System.out.println("redo cannot be done at this stage ");
                                System.out.println("Press 1 to undo the move");
                                switch (sc.nextInt()) {
                                    case 1 -> {
                                        m.undo(new Snapshot(turn.getCurrentTurn()));
                                        Board.displayundoredo(m.getUndoStack().peek().getBoardstring());
                                    }
                                    case 2 -> {
                                        m.redo(new Snapshot(turn.getCurrentTurn()));
                                        Board.displayundoredo(m.getUndoStack().peek().getBoardstring());
                                    }
                                }
                            }else if(m.canUndo()){
                                System.out.println("Press 1 to undo the move");
                                System.out.println("Press 2 to redo the move");
                                switch (sc.nextInt()) {
                                    case 1 -> {
                                        m.undo(new Snapshot(turn.getCurrentTurn()));
                                        Board.displayundoredo(m.getUndoStack().peek().getBoardstring());
                                    }
                                    case 2 -> {

                                    }
                                }
                            }


                        }
                        case 2 -> {break;}
                    }
                }
                System.out.println();
                System.out.println();
                board.displayBoard();
                System.out.println();
                System.out.println();

            }
            else{

                    if (Board.isCheckmate(Color.BLACK)) {
                        System.out.println("Checkmate, " + pl[0].getPlayername() + "wins the game ");
                        win = true;
                    }
                    System.out.println(turn.getCurrentTurn() + "'s turn has started");

                    if (Board.findpiecebypos(sc.next()).move(moveto = sc.next())) {
                        record.addMove(pl[1].getPlayername(), pl[1].getPieces()[piecenum].getPos(), moveto);
                        turn.endTurn();
                        m.recordAfterMove(new Snapshot(turn.getCurrentTurn()));
                        snapshot.savesnapshopt(Board.getBoard(), turn.getCurrentTurn());
                    } else {
                        System.out.println("Invalid move, please try again");
                        if (Board.findpiecebypos(sc.next()).move(moveto = sc.next())) {
                            record.addMove(pl[1].getPlayername(), pl[1].getPieces()[piecenum].getPos(), moveto);
                            turn.endTurn();
                            m.recordAfterMove(new Snapshot(turn.getCurrentTurn()));
                            snapshot.savesnapshopt(Board.getBoard(), turn.getCurrentTurn());
                        }
                    }
                    System.out.println();
                    System.out.println();
                    System.out.println();

                    board.displayBoard();
                }
            } while (!win) ;


            // Prints the record of moves played in the game
            record.printHistory();


            // Example: let's assume player 0 (Ali) won, player 1 (Dani) lost
            System.out.println("\nGame Over!");
            System.out.println("Enter winner's name: ");
            String winnerName = sc.next();

            // Update ratings based on winner
            for (Player p : pl) {
                if (p != null) {
                    if (p.getPlayername().equalsIgnoreCase(winnerName)) {
                        Players.updatePlayer(p.getPlayername(), 50, true);   // win: +50 rating
                    } else {
                        Players.updatePlayer(p.getPlayername(), -20, false);  // lose: -20 rating
                    }
                }

            }

            // Refresh leaderboard
            LeaderBoard.updateBoard();

            // Display leaderboard
            LeaderBoard.printBoard();

            sc.close();

        }
    }


