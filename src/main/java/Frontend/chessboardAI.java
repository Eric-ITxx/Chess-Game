package Frontend;

import gamelogic.*;
import gamelogic.Color;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

/**
 * GUI chessboard where the human plays WHITE and Stockfish plays BLACK.
 */
public class chessboardAI {

    private static final int SIZE       = 80;
    private static final int BOARD_SIZE = 8;
    private static final String FILES   = "abcdefgh";
    private static final String BG_PATH = "file:src/main/resources/Dash.png";

    // Board color scheme — silverish black
    private static final javafx.scene.paint.Color LIGHT_SQ  = javafx.scene.paint.Color.rgb(180, 188, 196);
    private static final javafx.scene.paint.Color DARK_SQ   = javafx.scene.paint.Color.rgb(34, 34, 34);
    private static final javafx.scene.paint.Color SEL_COLOR = javafx.scene.paint.Color.rgb(255, 210, 30, 0.9);

    // Stockfish think time per difficulty
    private static final int EASY_MS   = 150;
    private static final int MEDIUM_MS = 1000;
    private static final int HARD_MS   = 4000;

    // ── Backend ──
    private Board        gameBoard;
    private Player       whitePlayer;
    private Player       blackPlayer;
    private Playerturn   turnManager;
    private MoveManager  moveManager;
    private Snapshot     snapshotSaver;
    private StockfishEngine stockfish;
    private int          thinkTimeMs = MEDIUM_MS;

    // ── UI ──
    private GridPane     boardGrid;
    private StackPane[][]  cells     = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private Rectangle[][]  cellRects = new Rectangle[BOARD_SIZE][BOARD_SIZE];
    private StackPane    pauseOverlay;
    private Text         turnText;
    private Text         statusText;
    private boolean      paused     = false;
    private boolean      aiThinking = false;

    // ── Animation ──
    private Pane         animationLayer;

    // ── Selection ──
    private piece        selectedPiece = null;

    // ── Stage / user ──
    private Stage        stageRef;
    private String       username = "Player";
    private int          rating   = 0;

    // ── Images ──
    private Image whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteKing;
    private Image blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackKing;

    // ─────────────────────────────── Entry point ───────────────────────────────

    public void start(Stage stage) {
        this.stageRef = stage;

        // Ask difficulty before starting
        thinkTimeMs = askDifficulty();

        gameBoard    = new Board();
        whitePlayer  = new Player(Color.WHITE, username);
        blackPlayer  = new Player(Color.BLACK, "Stockfish 18");
        turnManager  = new Playerturn(Color.WHITE);
        moveManager  = new MoveManager();

        try { moveManager.recordInitial(new Snapshot(Color.WHITE)); } catch (Exception ignored) {}

        // Start Stockfish engine on a background thread so the UI doesn't freeze
        new Thread(() -> {
            stockfish = new StockfishEngine();
            boolean ok = stockfish.start();
            if (!ok) {
                Platform.runLater(() -> showAlert(
                    "Stockfish not found!\nExpected: src/main/resources/stockfish.exe\n" +
                    "Download it from stockfishchess.org and place it there."));
            }
        }).start();

        buildBoard();
        loadPieceImages();
        refreshBoardFromModel();

        pauseOverlay = buildPauseOverlay();
        VBox buttons = buildRightButtons();

        animationLayer = new Pane();
        animationLayer.setMouseTransparent(true);

        BorderPane root = new BorderPane();
        root.setCenter(new StackPane(boardGrid, pauseOverlay, animationLayer));
        root.setRight(buttons);
        root.setStyle(
            "-fx-background-image: url('" + BG_PATH + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center;"
        );

        Scene scene = new Scene(root, 1100, 780);
        stage.setTitle("Chess — Player vs Stockfish 18");
        stage.setScene(scene);
        stage.show();

        updateTurnText();
    }

    // ─────────────────────────────── Difficulty ───────────────────────────────

    private int askDifficulty() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Choose Difficulty",
                "How hard do you want Stockfish to play?",
                "Easy", "Medium", "Hard");
        return switch (choice) {
            case "Easy" -> EASY_MS;
            case "Hard" -> HARD_MS;
            default     -> MEDIUM_MS;
        };
    }

    // ─────────────────────────────── Board UI ───────────────────────────────

    private void buildBoard() {
        boardGrid = new GridPane();
        boardGrid.setPadding(new Insets(10, 20, 20, 20));
        boardGrid.setAlignment(Pos.CENTER);

        Font coordFont = Font.font(14);

        // File labels (a-h)
        for (int c = 0; c < BOARD_SIZE; c++) {
            Label lbl = new Label(String.valueOf(FILES.charAt(c)));
            lbl.setMinWidth(SIZE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setTextFill(Paint.valueOf("white"));
            lbl.setFont(coordFont);
            lbl.setTranslateY(-8);
            boardGrid.add(lbl, c + 1, 0);
        }

        // Rank labels (8-1)
        for (int r = 0; r < BOARD_SIZE; r++) {
            Label lbl = new Label(String.valueOf(8 - r));
            lbl.setMinHeight(SIZE);
            lbl.setAlignment(Pos.CENTER);
            lbl.setTextFill(Paint.valueOf("white"));
            lbl.setFont(coordFont);
            lbl.setTranslateY(-6);
            boardGrid.add(lbl, 0, r + 1);
        }

        // Cells
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Rectangle rect = new Rectangle(SIZE, SIZE);
                rect.setFill((r + c) % 2 == 0 ? LIGHT_SQ : DARK_SQ);
                rect.setStroke(javafx.scene.paint.Color.rgb(15, 15, 15));

                StackPane cell = new StackPane(rect);
                int rr = r, cc = c;
                cell.setOnMouseClicked(e -> handleHumanClick(rr, cc));

                cells[r][c]     = cell;
                cellRects[r][c] = rect;
                boardGrid.add(cell, c + 1, r + 1);
            }
        }
    }

    private VBox buildRightButtons() {
        turnText   = new Text();
        statusText = new Text("Your turn (White)");
        turnText.setFill(javafx.scene.paint.Color.WHITE);
        turnText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        statusText.setFill(javafx.scene.paint.Color.YELLOW);
        statusText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Button pause  = styledButton("Pause");
        Button resume = styledButton("Resume");
        Button undo   = styledButton("Undo");
        Button redo   = styledButton("Redo");
        Button save   = styledButton("Save");
        Button back   = styledButton("Back");
        Button exit   = styledButton("Exit");

        pause .setOnAction(e -> { paused = true;  pauseOverlay.setVisible(true);  });
        resume.setOnAction(e -> { paused = false; pauseOverlay.setVisible(false); });
        undo  .setOnAction(e -> doUndo());
        redo  .setOnAction(e -> doRedo());
        save  .setOnAction(e -> {
            try {
                snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn());
                showAlert("Game saved.");
            } catch (Exception ex) { showAlert("Save failed."); }
        });
        back.setOnAction(e -> {
            shutdownStockfish();
            openDashboard();
        });
        exit.setOnAction(e -> {
            shutdownStockfish();
            if (stageRef != null) stageRef.close();
        });

        VBox box = new VBox(12, turnText, statusText,
                pause, resume, undo, redo, save, back, exit);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6));
        box.setPrefWidth(240);
        box.setStyle("-fx-background-color: transparent;");
        return box;
    }

    private Button styledButton(String text) {
        Button b = new Button(text);
        b.setPrefWidth(220);
        b.setPrefHeight(52);
        b.setStyle(
            "-fx-font-size: 16; -fx-font-weight: bold;" +
            "-fx-background-color: rgba(60,60,60,0.85);" +
            "-fx-text-fill: white; -fx-background-radius: 10;"
        );
        return b;
    }

    private StackPane buildPauseOverlay() {
        Rectangle bg = new Rectangle(900, 740,
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.55));
        Text text = new Text("PAUSED");
        text.setFill(javafx.scene.paint.Color.WHITE);
        text.setStyle("-fx-font-size: 52; -fx-font-weight: bold;");
        StackPane overlay = new StackPane(bg, text);
        overlay.setVisible(false);
        return overlay;
    }

    // ─────────────────────────────── Images ───────────────────────────────

    private void loadPieceImages() {
        whitePawn   = img("file:src/main/resources/Pawn.jpeg");
        whiteRook   = img("file:src/main/resources/white rook.jpeg");
        whiteKnight = img("file:src/main/resources/white horse.jpeg");
        whiteBishop = img("file:src/main/resources/white bishop.jpeg");
        whiteQueen  = img("file:src/main/resources/white queen.jpeg");
        whiteKing   = img("file:src/main/resources/white king.jpeg");
        blackPawn   = img("file:src/main/resources/black pawn.jpeg");
        blackRook   = img("file:src/main/resources/black rook.jpeg");
        blackKnight = img("file:src/main/resources/black horse.jpeg");
        blackBishop = img("file:src/main/resources/black bishop.jpeg");
        blackQueen  = img("file:src/main/resources/black queen.jpeg");
        blackKing   = img("file:src/main/resources/black king.jpeg");
    }

    private Image img(String path) {
        try { return new Image(path); } catch (Exception e) { return null; }
    }

    private Image getImageForPiece(piece p) {
        if (p.getColor() == Color.WHITE) {
            return switch (p.getType()) {
                case PAWN -> whitePawn; case ROOK -> whiteRook;
                case KNIGHT -> whiteKnight; case BISHOP -> whiteBishop;
                case QUEEN -> whiteQueen; case KING -> whiteKing;
            };
        } else {
            return switch (p.getType()) {
                case PAWN -> blackPawn; case ROOK -> blackRook;
                case KNIGHT -> blackKnight; case BISHOP -> blackBishop;
                case QUEEN -> blackQueen; case KING -> blackKing;
            };
        }
    }

    // ─────────────────────────────── Rendering ───────────────────────────────

    private void refreshBoardFromModel() {
        for (StackPane[] row : cells)
            for (StackPane cell : row)
                cell.getChildren().removeIf(n ->
                    "pieceNode".equals(n.getId()) || "legalDot".equals(n.getId()) || n instanceof ImageView);

        piece[][] model = Board.getBoard();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (model[r][c] != null)
                    cells[r][c].getChildren().add(
                        PieceRenderer.createPiece(model[r][c].getType(), model[r][c].getColor(), SIZE));
    }

    private void resetColors() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                cellRects[r][c].setFill((r + c) % 2 == 0 ? LIGHT_SQ : DARK_SQ);
                cells[r][c].getChildren().removeIf(n -> "legalDot".equals(n.getId()));
            }
    }

    // ─────────────────────────────── Human move ───────────────────────────────

    private void handleHumanClick(int r, int c) {
        if (paused || aiThinking) return;
        if (turnManager.getCurrentTurn() != Color.WHITE) return;

        piece[][] model = Board.getBoard();
        piece clicked   = model[r][c];
        resetColors();

        if (selectedPiece == null) {
            if (clicked != null && clicked.getColor() == Color.WHITE) {
                selectedPiece = clicked;
                cellRects[r][c].setFill(SEL_COLOR);
                List<String> moves = clicked.getPossibleMoves(model);
                for (String mv : moves) {
                    if (Board.isMoveLegal(clicked, mv)) {
                        int[] rc = piece.algebraicToRC(mv);
                        boolean isCapture = model[rc[0]][rc[1]] != null;
                        Circle dot = new Circle(isCapture ? 34 : 11);
                        if (isCapture) {
                            dot.setFill(javafx.scene.paint.Color.TRANSPARENT);
                            dot.setStroke(javafx.scene.paint.Color.rgb(80, 220, 80, 0.75));
                            dot.setStrokeWidth(4);
                        } else {
                            dot.setFill(javafx.scene.paint.Color.rgb(80, 220, 80, 0.55));
                        }
                        dot.setMouseTransparent(true);
                        dot.setId("legalDot");
                        cells[rc[0]][rc[1]].getChildren().add(dot);
                    }
                }
            }
            return;
        }

        if (clicked != null && clicked.getColor() == Color.WHITE) {
            selectedPiece = null;
            handleHumanClick(r, c);
            return;
        }

        int fromR = selectedPiece.getRow();
        int fromC = selectedPiece.getCol();
        piecetype movingType    = selectedPiece.getType();
        Color     movingColor   = selectedPiece.getColor();

        String target = piece.rcToAlgebraic(r, c);
        boolean moved = selectedPiece.move(target);
        selectedPiece = null;

        if (moved) {
            Node animNode = PieceRenderer.createPiece(movingType, movingColor, SIZE);
            aiThinking = true;
            animatePieceMove(fromR, fromC, r, c, animNode, () -> {
                refreshBoardFromModel();
                aiThinking = false;

                if (Board.isCheckmate(Color.BLACK)) {
                    ChessDialog.showInfo(stageRef, "Checkmate!", "You win! Stockfish is defeated.");
                    boardGrid.setDisable(true);
                    return;
                }

                turnManager.endTurn();
                try {
                    moveManager.recordAfterMove(new Snapshot(Color.BLACK));
                    if (snapshotSaver == null) snapshotSaver = new Snapshot(Color.BLACK);
                    snapshotSaver.savesnapshopt(Board.getBoard(), Color.BLACK);
                } catch (Exception ignored) {}

                updateTurnText();
                triggerAIMove();
            });
        }
    }

    // ─────────────────────────────── AI move ───────────────────────────────

    private void triggerAIMove() {
        if (stockfish == null) return;

        aiThinking = true;
        setStatus("Stockfish is thinking...");

        // Snapshot the board for FEN (must be done on FX thread before handing off)
        piece[][] snapshot = Board.getBoard();
        String fen = StockfishEngine.boardToFen(snapshot, Color.BLACK);

        new Thread(() -> {
            try {
                String uci = stockfish.getBestMove(fen, thinkTimeMs);
                Platform.runLater(() -> applyAIMove(uci));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    aiThinking = false;
                    setStatus("Stockfish error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void applyAIMove(String uci) {
        if (uci == null) {
            aiThinking = false;
            ChessDialog.showInfo(stageRef, "Game Over", "Stockfish has no moves — you win!");
            boardGrid.setDisable(true);
            return;
        }

        // Find the piece BEFORE applying the move so we can animate it
        Network.Move m = StockfishEngine.uciToMove(uci);
        piece[][] boardState = Board.getBoard();
        piece movingPiece = (m != null) ? boardState[m.fromRow][m.fromCol] : null;
        Node movingNode = (movingPiece != null)
                ? PieceRenderer.createPiece(movingPiece.getType(), movingPiece.getColor(), SIZE) : null;

        boolean moved = StockfishEngine.applyUciMove(uci);
        if (!moved) {
            System.out.println("AI move not applied: " + uci);
            aiThinking = false;
            turnManager.endTurn();
            updateTurnText();
            setStatus("Your turn (White)");
            return;
        }

        int toR = m.toRow, toC = m.toCol;
        int fromR = m.fromRow, fromC = m.fromCol;

        animatePieceMove(fromR, fromC, toR, toC, movingNode, () -> {
            refreshBoardFromModel();
            aiThinking = false;

            if (Board.isCheckmate(Color.WHITE)) {
                ChessDialog.showInfo(stageRef, "Checkmate!", "Stockfish wins. Better luck next time!");
                boardGrid.setDisable(true);
                return;
            }

            turnManager.endTurn();
            try {
                moveManager.recordAfterMove(new Snapshot(Color.WHITE));
                if (snapshotSaver == null) snapshotSaver = new Snapshot(Color.WHITE);
                snapshotSaver.savesnapshopt(Board.getBoard(), Color.WHITE);
            } catch (Exception ignored) {}

            updateTurnText();
            setStatus("Your turn (White)");
        });
    }

    // ─────────────────────────────── Animation ───────────────────────────────

    private void animatePieceMove(int fromR, int fromC, int toR, int toC,
                                   Node pieceNode, Runnable onComplete) {
        if (pieceNode == null || animationLayer == null) { onComplete.run(); return; }

        cells[fromR][fromC].getChildren().removeIf(n -> "pieceNode".equals(n.getId()));

        Bounds fromB    = cells[fromR][fromC].localToScene(cells[fromR][fromC].getBoundsInLocal());
        Bounds toB      = cells[toR][toC].localToScene(cells[toR][toC].getBoundsInLocal());
        Bounds overlayB = animationLayer.localToScene(animationLayer.getBoundsInLocal());

        double startX = fromB.getMinX() - overlayB.getMinX();
        double startY = fromB.getMinY() - overlayB.getMinY();
        double endX   = toB.getMinX()   - overlayB.getMinX();
        double endY   = toB.getMinY()   - overlayB.getMinY();

        pieceNode.setLayoutX(startX);
        pieceNode.setLayoutY(startY);
        animationLayer.getChildren().add(pieceNode);

        TranslateTransition tt = new TranslateTransition(Duration.millis(320), pieceNode);
        tt.setByX(endX - startX);
        tt.setByY(endY - startY);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> {
            animationLayer.getChildren().remove(pieceNode);
            onComplete.run();
        });
        tt.play();
    }

    // ─────────────────────────────── Undo / Redo ───────────────────────────────

    private void doUndo() {
        if (paused || aiThinking) return;
        if (moveManager == null || !moveManager.canUndo()) return;
        try {
            Snapshot prev = moveManager.undo(new Snapshot(turnManager.getCurrentTurn()));
            if (prev != null) {
                Board.displayundoredo(prev.getBoardstring());
                if (prev.getSnapshotColor() != null)
                    turnManager = new Playerturn(prev.getSnapshotColor());
                refreshBoardFromModel();
                updateTurnText();
            }
        } catch (Exception ignored) {}
    }

    private void doRedo() {
        if (paused || aiThinking) return;
        if (moveManager == null || !moveManager.canRedo()) return;
        try {
            Snapshot next = moveManager.redo(new Snapshot(turnManager.getCurrentTurn()));
            if (next != null) {
                Board.displayundoredo(next.getBoardstring());
                if (next.getSnapshotColor() != null)
                    turnManager = new Playerturn(next.getSnapshotColor());
                refreshBoardFromModel();
                updateTurnText();
            }
        } catch (Exception ignored) {}
    }

    // ─────────────────────────────── Helpers ───────────────────────────────

    private void updateTurnText() {
        if (turnText != null)
            turnText.setText("Turn: " + turnManager.getCurrentTurn());
    }

    private void setStatus(String msg) {
        if (statusText != null) statusText.setText(msg);
    }

    private void showAlert(String msg) {
        ChessDialog.showInfo(stageRef, "Chess", msg);
    }

    private void openDashboard() {
        try {
            DashBoardView dash = new DashBoardView(username, rating, stageRef);
            Scene dashScene = new Scene(dash, 900, 700);
            stageRef.setTitle("Chess - Dashboard");
            stageRef.setScene(dashScene);
        } catch (Exception e) {
            showAlert("Could not open dashboard.");
        }
    }

    private void shutdownStockfish() {
        if (stockfish != null) {
            new Thread(() -> stockfish.stop()).start();
        }
    }
}
