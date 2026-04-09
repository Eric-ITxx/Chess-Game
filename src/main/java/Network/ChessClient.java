package Network;

import gamelogic.*;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChessClient {

    private static final int PORT = 5000;

    public static void startJoin(String hostIp) {
        System.out.println("=== CHESS LAN (CLIENT / BLACK) ===");

        try (
                Socket socket = new Socket(hostIp, PORT);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                Scanner sc = new Scanner(System.in)
        ) {
            System.out.println("Connected to host: " + socket.getInetAddress());

            // ---- Game Setup ----
            Board board = new Board();
            Player white = new Player(Color.WHITE, "HOST (White)");
            Player black = new Player(Color.BLACK, "YOU (Black)");

            boolean whitesTurn = true;

            // ---- Game Loop ----
            while (true) {
                showBoard(board);

                if (whitesTurn) {
                    if (!receiveOpponentMove(in, white)) break;
                } else {
                    if (!makeMyMove(sc, out, black)) continue;
                }

                whitesTurn = !whitesTurn;
            }

        } catch (IOException e) {
            System.out.println("Connection lost.");
        }
    }

    // ================= HELPER METHODS =================

    private static void showBoard(Board board) {
        System.out.println("\nCurrent Board:");
        board.displayBoard();
    }

    private static boolean receiveOpponentMove(BufferedReader in, Player white)
            throws IOException {

        System.out.println("\nWaiting for opponent move...");
        String line = in.readLine();

        if (line == null) {
            System.out.println("Host disconnected.");
            return false;
        }

        String[] move = line.split("\\s+");
        if (move.length != 2) {
            System.out.println("Invalid move received.");
            return false;
        }

        System.out.println("Opponent move: " + move[0] + " -> " + move[1]);

        if (!applyMove(white, move[0], move[1])) {
            System.out.println("Opponent sent illegal move.");
            return false;
        }

        return true;
    }

    private static boolean makeMyMove(Scanner sc, PrintWriter out, Player black) {
        System.out.print("\nYOUR MOVE (from to) e.g. g7 g5 : ");

        String from = sc.next();
        String to = sc.next();

        if (!applyMove(black, from, to)) {
            System.out.println("Invalid move. Try again.");
            return false;
        }

        out.println(from + " " + to);
        return true;
    }

    private static boolean applyMove(Player player, String from, String to) {
        piece p = findPieceByPos(player, from);
        return p != null && p.move(to);
    }

    private static piece findPieceByPos(Player player, String from) {
        for (piece p : player.getPieces()) {
            if (p != null && p.getPos().equalsIgnoreCase(from)) {
                return p;
            }
        }
        return null;
    }
}
