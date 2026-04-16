package gamelogic;

import Network.Move;
import java.io.*;

/**
 * Wraps the Stockfish chess engine via the UCI protocol.
 *
 * Coordinate mapping:
 *   This game's board uses rows 0-7 where row 0 = black's back rank (standard rank 8)
 *   and row 7 = white's back rank (standard rank 1).
 *   Conversion: standard_rank = 8 - their_row
 */
public class StockfishEngine {

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;

    // ─────────────────────── Lifecycle ───────────────────────

    private static File extractStockfish() throws IOException {
        try (InputStream in = StockfishEngine.class.getResourceAsStream("/stockfish.exe")) {
            if (in == null) throw new IOException("stockfish.exe not found in resources");
            File temp = File.createTempFile("stockfish", ".exe");
            temp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(temp)) {
                in.transferTo(out);
            }
            temp.setExecutable(true);
            return temp;
        }
    }

    public boolean start() {
        try {
            File exe = extractStockfish();
            process = new ProcessBuilder(exe.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            sendCommand("uci");
            waitFor("uciok");
            sendCommand("isready");
            waitFor("readyok");
            sendCommand("ucinewgame");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start Stockfish: " + e.getMessage());
            return false;
        }
    }

    public void stop() {
        try {
            if (writer != null) sendCommand("quit");
        } catch (Exception ignored) {}
        if (process != null) process.destroyForcibly();
    }

    // ─────────────────────── UCI communication ───────────────────────

    private void sendCommand(String cmd) throws IOException {
        writer.write(cmd + "\n");
        writer.flush();
    }

    private void waitFor(String token) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(token)) return;
        }
    }

    /**
     * Ask Stockfish for the best move given the current board state.
     *
     * @param fen         FEN string of the position
     * @param thinkTimeMs how long Stockfish gets to think (milliseconds)
     * @return UCI move string like "e2e4", or null if none
     */
    public String getBestMove(String fen, int thinkTimeMs) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + thinkTimeMs);
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("bestmove")) {
                String[] parts = line.split(" ");
                return parts.length > 1 && !parts[1].equals("(none)") ? parts[1] : null;
            }
        }
        return null;
    }

    // ─────────────────────── FEN generation ───────────────────────

    /**
     * Converts the current board state to a FEN string.
     *
     * Their row 0 = standard rank 8 (black back rank)
     * Their row 7 = standard rank 1 (white back rank)
     * FEN reads from rank 8 → rank 1 (i.e., their row 0 → row 7), so we iterate rows 0..7.
     */
    public static String boardToFen(piece[][] board, Color turn) {
        StringBuilder fen = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            int empty = 0;
            for (int c = 0; c < 8; c++) {
                piece p = board[r][c];
                if (p == null) {
                    empty++;
                } else {
                    if (empty > 0) { fen.append(empty); empty = 0; }
                    fen.append(toFenChar(p));
                }
            }
            if (empty > 0) fen.append(empty);
            if (r < 7) fen.append('/');
        }

        fen.append(turn == Color.WHITE ? " w" : " b");
        fen.append(" - - 0 1"); // no castling/en-passant tracking (game doesn't support it reliably)
        return fen.toString();
    }

    private static char toFenChar(piece p) {
        char sym = p.getSymbol();
        // Symbols are already uppercase for white, lowercase for black — matches FEN exactly.
        // Exception: Knight should be 'N'/'n', not 'H'/'h'.
        if (p.getType() == piecetype.KNIGHT) {
            return p.getColor() == Color.WHITE ? 'N' : 'n';
        }
        return sym;
    }

    // ─────────────────────── Move conversion ───────────────────────

    /**
     * Convert a UCI move string (e.g. "e2e4") to this game's internal Move.
     *
     * UCI uses standard ranks 1-8. This game uses rows 0-7 where row = 8 - standard_rank.
     *
     * So "e2e4":
     *   from: file='e'(col=4), rank=2 → their row = 8-2 = 6
     *   to:   file='e'(col=4), rank=4 → their row = 8-4 = 4
     */
    public static Move uciToMove(String uci) {
        int fromCol = uci.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(uci.charAt(1));
        int toCol   = uci.charAt(2) - 'a';
        int toRow   = 8 - Character.getNumericValue(uci.charAt(3));
        return new Move(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Apply a UCI move to the live board by finding the piece and calling piece.move().
     *
     * @return true if the move was applied successfully
     */
    public static boolean applyUciMove(String uci) {
        if (uci == null || uci.length() < 4) return false;

        Move m = uciToMove(uci);
        piece[][] board = Board.getBoard();
        piece p = board[m.fromRow][m.fromCol];
        if (p == null) return false;

        // Convert destination to this game's algebraic notation: file + row-index
        String target = piece.rcToAlgebraic(m.toRow, m.toCol);
        return p.move(target);
    }
}
