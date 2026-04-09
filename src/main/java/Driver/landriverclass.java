package Driver;

import gamelogic.*;
import makingstats.*;
import Network.ChessClient;
import Network.ChessServer;
import Network.Move;

import java.util.Scanner;

public class landriverclass {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("===== CHESS GAME =====");
        System.out.println("1) Local Game");
        System.out.println("2) LAN Game");
        System.out.print("Choose mode: ");
        int modeChoice = sc.nextInt();

        if (modeChoice == 1) {
            startLocalGame(sc);
        }
        else if (modeChoice == 2) {
            startLanGame(sc);
        }
        else {
            System.out.println("Invalid choice.");
        }

        sc.close();
    }

    // ======================= LOCAL GAME =======================
    private static void startLocalGame(Scanner sc) {

        System.out.println("Do you want to play against the computer? (yes/no): ");
        boolean vsComputer = sc.next().equalsIgnoreCase("yes");

        boolean checkmate = false;
        Board board = new Board();
        History record = new History();

        Player[] pl = new Player[2];
        pl[0] = new Player(Color.WHITE, "Player 1");
        pl[1] = vsComputer
                ? new Player(Color.BLACK, "Computer")
                : new Player(Color.BLACK, "Player 2");

        Players.addPlayer(pl[0].getPlayername(), 600);
        Players.addPlayer(pl[1].getPlayername(), 800);

        Playerturn turn = new Playerturn(Color.WHITE);
        ComputerPlayer ai = vsComputer ? new ComputerPlayer() : null;

        board.displayBoard();

        do {
            Color current = turn.getCurrentTurn();
            int playerIndex = (current == Color.WHITE) ? 0 : 1;

            if (playerIndex == 0 || !vsComputer) {
                System.out.println(current + "'s turn (Human)");

                while (true) {
                    System.out.print("Enter piece number and target square (e.g., 0 e4): ");
                    int pieceNum = sc.nextInt();
                    String moveTo = sc.next();

                    if (pl[playerIndex].getPieces()[pieceNum].move(moveTo)) {
                        record.addMove(
                                pl[playerIndex].getPlayername(),
                                pl[playerIndex].getPieces()[pieceNum].getPos(),
                                moveTo
                        );
                        turn.endTurn();
                        break;
                    } else {
                        System.out.println("Invalid move, try again.");
                    }
                }

            } else {
                System.out.println("Computer's turn");
                Move aiMove = ai.getNextMove(board, Color.BLACK);

                if (aiMove == null) {
                    System.out.println("No valid moves. Game over.");
                    break;
                }

                piece p = Board.getBoard()[aiMove.fromRow][aiMove.fromCol];
                String toSq = p.rcToAlgebraic(aiMove.toRow, aiMove.toCol);
                p.move(toSq);
                record.addMove(pl[1].getPlayername(), p.getPos(), toSq);
                turn.endTurn();
                System.out.println("Computer moved to " + toSq);
            }

            board.displayBoard();

            if (Board.isCheckmate(current)) {
                System.out.println("Checkmate!");
                checkmate = true;
            }

        } while (!checkmate);

        record.printHistory();
    }

    // ======================= LAN GAME =======================
    private static void startLanGame(Scanner sc) {

        System.out.println("1) Host Game =>(White)");
        System.out.println("2) Join Game =>(Black)");
        System.out.print("Choose: ");
        int choice = sc.nextInt();

        if (choice == 1) {
            ChessServer.startHost();
        }
        else if (choice == 2) {
            System.out.print("Enter Host IP: ");
            String ip = sc.next();
            ChessClient.startJoin(ip);
        }
        else {
            System.out.println("Invalid LAN option.");
        }
    }
}
