package Frontend;

import gamelogic.*;
import gamelogic.Color;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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

import java.util.ArrayList;
import java.util.List;

/**
 * GUI chessboard where the human plays WHITE and Stockfish plays BLACK.
 */
public class chessboardAI {

    private static final int SIZE       = 80;
    private static final int BOARD_SIZE = 8;
    private static final String FILES   = "abcdefgh";
    private static final String BG_PATH = chessboardAI.class.getResource("/Dash.png").toExternalForm();

    private static final javafx.scene.paint.Color SEL_COLOR       = javafx.scene.paint.Color.rgb(255, 210, 30, 0.9);
    private static final javafx.scene.paint.Color LAST_MOVE_COLOR = javafx.scene.paint.Color.rgb(255, 220, 50, 0.35);

    private static final int EASY_MS   = 150;
    private static final int MEDIUM_MS = 1000;
    private static final int HARD_MS   = 4000;

    // ── Backend ──
    private Board          gameBoard;
    private Player         whitePlayer;
    private Player         blackPlayer;
    private Playerturn     turnManager;
    private MoveManager    moveManager;
    private Snapshot       snapshotSaver;
    private StockfishEngine stockfish;
    private int            thinkTimeMs = MEDIUM_MS;

    // ── UI ──
    private GridPane       boardGrid;
    private StackPane[][]  cells     = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private Rectangle[][]  cellRects = new Rectangle[BOARD_SIZE][BOARD_SIZE];
    private StackPane      pauseOverlay;
    private Text           turnText;
    private Text           statusText;
    private boolean        paused    = false;
    private boolean        aiThinking = false;

    // ── Animation ──
    private Pane animationLayer;

    // ── Clock ──
    private ChessClock clock;

    // ── Selection ──
    private piece selectedPiece = null;

    // ── Stage / user ──
    private Stage  stageRef;
    private String username = "Player";
    private int    rating   = 0;

    // ── Last move highlight ──
    private int lastFromR = -1, lastFromC = -1, lastToR = -1, lastToC = -1;

    // ── Move history ──
    private ListView<String> moveHistoryView;
    private int halfMoveCount = 0;

    // ── Captured pieces ──
    private final List<piecetype> capturedByWhite = new ArrayList<>(); // black pieces white captured
    private final List<piecetype> capturedByBlack = new ArrayList<>(); // white pieces black captured
    private Label capturedTopLabel;
    private Label capturedBottomLabel;

    // ── Images (kept for compatibility) ──
    private Image whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteKing;
    private Image blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackKing;

    // Public setters
    public void setUsername(String u) { this.username = u; }
    public void setRating(int r)      { this.rating = r;   }

    // ─────────────────────────────── Entry point ───────────────────────────────

    public void start(Stage stage) {
        this.stageRef = stage;

        thinkTimeMs  = askDifficulty();
        askTimeControl();

        gameBoard    = new Board();
        whitePlayer  = new Player(Color.WHITE, username);
        blackPlayer  = new Player(Color.BLACK, "Stockfish 18");
        turnManager  = new Playerturn(Color.WHITE);
        moveManager  = new MoveManager();
        try { moveManager.recordInitial(new Snapshot(Color.WHITE)); } catch (Exception ignored) {}

        new Thread(() -> {
            stockfish = new StockfishEngine();
            boolean ok = stockfish.start();
            if (!ok) Platform.runLater(() -> showAlert(
                "Stockfish not found!\nExpected: src/main/resources/stockfish.exe\n" +
                "Download it from stockfishchess.org and place it there."));
        }).start();

        buildBoard();
        loadPieceImages();
        refreshBoardFromModel();

        pauseOverlay   = buildPauseOverlay();
        VBox buttons   = buildRightButtons();
        animationLayer = new Pane();
        animationLayer.setMouseTransparent(true);

        capturedTopLabel    = buildCapturedLabel();
        capturedBottomLabel = buildCapturedLabel();

        VBox boardArea = new VBox(4,
                capturedTopLabel,
                new StackPane(boardGrid, pauseOverlay, animationLayer),
                capturedBottomLabel);
        boardArea.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(boardArea);
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

        if (clock != null) clock.startWhite();
        updateTurnText();
    }

    // ─────────────────────────────── Time control ───────────────────────────────

    private void askTimeControl() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Time Control", "Choose a time control:",
                "Bullet  1 min", "Rapid  10 min", "Classical  30 min", "No Limit");
        long secs = switch (choice) {
            case "Bullet  1 min"     -> 60L;
            case "Rapid  10 min"     -> 600L;
            case "Classical  30 min" -> 1800L;
            default -> 0L;
        };
        if (secs > 0) {
            clock = new ChessClock(secs,
                () -> { boardGrid.setDisable(true); ChessDialog.showInfo(stageRef, "Time's Up!", "Stockfish wins — White ran out of time!"); },
                () -> { boardGrid.setDisable(true); ChessDialog.showInfo(stageRef, "Time's Up!", "You win — Stockfish ran out of time!"); }
            );
        }
    }

    // ─────────────────────────────── Difficulty ───────────────────────────────

    private int askDifficulty() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Choose Difficulty", "How hard do you want Stockfish to play?",
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
        for (int c = 0; c < BOARD_SIZE; c++) {
            Label lbl = new Label(String.valueOf(FILES.charAt(c)));
            lbl.setMinWidth(SIZE); lbl.setAlignment(Pos.CENTER);
            lbl.setTextFill(Paint.valueOf("white")); lbl.setFont(coordFont);
            lbl.setTranslateY(-8);
            boardGrid.add(lbl, c + 1, 0);
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            Label lbl = new Label(String.valueOf(8 - r));
            lbl.setMinHeight(SIZE); lbl.setAlignment(Pos.CENTER);
            lbl.setTextFill(Paint.valueOf("white")); lbl.setFont(coordFont);
            lbl.setTranslateY(-6);
            boardGrid.add(lbl, 0, r + 1);
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Rectangle rect = new Rectangle(SIZE, SIZE);
                rect.setFill((r + c) % 2 == 0 ? GameSettings.getLightSq() : GameSettings.getDarkSq());
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

    private Label buildCapturedLabel() {
        Label l = new Label();
        l.setStyle("-fx-font-size: 22; -fx-text-fill: white; -fx-padding: 2 20;");
        return l;
    }

    // ─────────────────────────────── Right panel ───────────────────────────────

    private VBox buildRightButtons() {
        turnText   = new Text();
        statusText = new Text("Your turn (White)");
        turnText.setFill(javafx.scene.paint.Color.WHITE);
        turnText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        statusText.setFill(javafx.scene.paint.Color.YELLOW);
        statusText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Text blackClock = (clock != null) ? clock.getBlackLabel() : new Text("Black  --:--");
        Text whiteClock = (clock != null) ? clock.getWhiteLabel() : new Text("White  --:--");
        blackClock.setFill(javafx.scene.paint.Color.WHITE); blackClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");
        whiteClock.setFill(javafx.scene.paint.Color.WHITE); whiteClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        moveHistoryView = new ListView<>();
        moveHistoryView.setPrefHeight(150);
        moveHistoryView.setPrefWidth(220);
        moveHistoryView.setStyle(
            "-fx-background-color: rgba(0,0,0,0.55);" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(255,255,255,0.2);" +
            "-fx-border-radius: 8;"
        );
        Label historyTitle = new Label("Move History");
        historyTitle.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13;");

        Button pause  = styledButton("Pause");
        Button resume = styledButton("Resume");
        Button undo   = styledButton("Undo");
        Button redo   = styledButton("Redo");
        Button save   = styledButton("Save");
        Button back   = styledButton("Back");
        Button exit   = styledButton("Exit");

        pause .setOnAction(e -> { paused = true;  pauseOverlay.setVisible(true);  if (clock != null) clock.pause();  });
        resume.setOnAction(e -> { paused = false; pauseOverlay.setVisible(false); if (clock != null) clock.resume(); });
        undo  .setOnAction(e -> doUndo());
        redo  .setOnAction(e -> doRedo());
        save  .setOnAction(e -> {
            try { snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn()); showAlert("Game saved."); }
            catch (Exception ex) { showAlert("Save failed."); }
        });
        back.setOnAction(e -> { shutdownStockfish(); openDashboard(); });
        exit.setOnAction(e -> { shutdownStockfish(); if (stageRef != null) stageRef.close(); });

        VBox box = new VBox(10, blackClock, turnText, whiteClock, statusText,
                historyTitle, moveHistoryView,
                pause, resume, undo, redo, save, back, exit);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6));
        box.setPrefWidth(240);
        box.setStyle("-fx-background-color: transparent;");
        return box;
    }

    private Button styledButton(String text) {
        Button b = new Button(text);
        b.setPrefWidth(220); b.setPrefHeight(46);
        b.setStyle(
            "-fx-font-size: 15; -fx-font-weight: bold;" +
            "-fx-background-color: rgba(60,60,60,0.85);" +
            "-fx-text-fill: white; -fx-background-radius: 10;"
        );
        return b;
    }

    private StackPane buildPauseOverlay() {
        Rectangle bg = new Rectangle(900, 740, javafx.scene.paint.Color.rgb(0, 0, 0, 0.55));
        Text text = new Text("PAUSED");
        text.setFill(javafx.scene.paint.Color.WHITE);
        text.setStyle("-fx-font-size: 52; -fx-font-weight: bold;");
        StackPane overlay = new StackPane(bg, text);
        overlay.setVisible(false);
        return overlay;
    }

    // ─────────────────────────────── Images ───────────────────────────────

    private void loadPieceImages() {
        whitePawn   = img("/Pawn.jpeg");        whiteRook   = img("/white rook.jpeg");
        whiteKnight = img("/white horse.jpeg"); whiteBishop = img("/white bishop.jpeg");
        whiteQueen  = img("/white queen.jpeg"); whiteKing   = img("/white king.jpeg");
        blackPawn   = img("/black pawn.jpeg");  blackRook   = img("/black rook.jpeg");
        blackKnight = img("/black horse.jpeg"); blackBishop = img("/black bishop.jpeg");
        blackQueen  = img("/black queen.jpeg"); blackKing   = img("/black king.jpeg");
    }

    private Image img(String path) {
        try { var in = getClass().getResourceAsStream(path); return in != null ? new Image(in) : null; }
        catch (Exception e) { return null; }
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
                javafx.scene.paint.Color base = (r + c) % 2 == 0
                        ? GameSettings.getLightSq() : GameSettings.getDarkSq();
                if ((r == lastFromR && c == lastFromC) || (r == lastToR && c == lastToC))
                    cellRects[r][c].setFill(LAST_MOVE_COLOR);
                else
                    cellRects[r][c].setFill(base);
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
                showLegalDots(clicked, model);
            }
            return;
        }

        if (clicked != null && clicked.getColor() == Color.WHITE) {
            selectedPiece = null;
            handleHumanClick(r, c);
            return;
        }

        // Capture tracking before move
        piece victim = model[r][c];

        int       fromR      = selectedPiece.getRow();
        int       fromC      = selectedPiece.getCol();
        piecetype movingType = selectedPiece.getType();
        Color     movingClr  = selectedPiece.getColor();

        String  target = piece.rcToAlgebraic(r, c);
        boolean moved  = selectedPiece.move(target);
        selectedPiece  = null;

        if (moved) {
            if (victim != null) {
                capturedByWhite.add(victim.getType());
                refreshCapturedBars();
            }

            addMoveToHistory(fromR, fromC, r, c, movingType, Color.WHITE);
            checkAndPromote(r, c, movingType, Color.WHITE);

            Node animNode = PieceRenderer.createPiece(movingType, movingClr, SIZE);
            aiThinking = true;
            animatePieceMove(fromR, fromC, r, c, animNode, () -> {
                refreshBoardFromModel();
                highlightLastMove(fromR, fromC, r, c);
                aiThinking = false;

                if (Board.isCheckmate(Color.BLACK)) {
                    showGameOver("You win! Stockfish is defeated.", true);
                    return;
                }
                if (Board.isStalemate(Color.BLACK)) {
                    showGameOver("Stalemate! It's a draw.", null);
                    return;
                }

                turnManager.endTurn();
                if (clock != null) clock.startBlack();
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

    // ─────────────────────────────── Legal move dots ───────────────────────────────

    private void showLegalDots(piece p, piece[][] model) {
        for (String mv : p.getPossibleMoves(model)) {
            if (Board.isMoveLegal(p, mv)) {
                int[] rc       = piece.algebraicToRC(mv);
                boolean isCapt = model[rc[0]][rc[1]] != null;
                Circle dot = new Circle(isCapt ? 34 : 11);
                if (isCapt) {
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

    // ─────────────────────────────── Pawn promotion ───────────────────────────────

    private void checkAndPromote(int r, int c, piecetype type, Color color) {
        if (type != piecetype.PAWN) return;
        boolean isPromotion = (color == Color.WHITE && r == 0) || (color == Color.BLACK && r == 7);
        if (!isPromotion) return;
        String choice = ChessDialog.showConfirm(stageRef, "Pawn Promotion!",
                "Choose promotion piece:", "Queen", "Rook", "Bishop", "Knight");
        piecetype newType = switch (choice) {
            case "Rook"   -> piecetype.ROOK;
            case "Bishop" -> piecetype.BISHOP;
            case "Knight" -> piecetype.KNIGHT;
            default       -> piecetype.QUEEN;
        };
        Board.promotePawn(r, c, newType);
    }

    // ─────────────────────────────── AI move ───────────────────────────────

    private void triggerAIMove() {
        if (stockfish == null) return;
        aiThinking = true;
        setStatus("Stockfish is thinking...");

        String fen = StockfishEngine.boardToFen(Board.getBoard(), Color.BLACK);
        new Thread(() -> {
            try {
                String uci = stockfish.getBestMove(fen, thinkTimeMs);
                Platform.runLater(() -> applyAIMove(uci));
            } catch (Exception e) {
                Platform.runLater(() -> { aiThinking = false; setStatus("Stockfish error: " + e.getMessage()); });
            }
        }).start();
    }

    private void applyAIMove(String uci) {
        if (uci == null) {
            aiThinking = false;
            showGameOver("Stockfish has no moves — you win!", true);
            return;
        }

        Network.Move m = StockfishEngine.uciToMove(uci);
        piece[][] boardState = Board.getBoard();

        // Track capture before applying move
        piece victim = (m != null) ? boardState[m.toRow][m.toCol] : null;
        if (victim != null) {
            capturedByBlack.add(victim.getType());
            refreshCapturedBars();
        }

        piece movingPiece = (m != null) ? boardState[m.fromRow][m.fromCol] : null;
        Node  movingNode  = (movingPiece != null)
                ? PieceRenderer.createPiece(movingPiece.getType(), movingPiece.getColor(), SIZE) : null;

        piecetype movingType = (movingPiece != null) ? movingPiece.getType() : piecetype.PAWN;

        boolean moved = StockfishEngine.applyUciMove(uci);
        if (!moved) {
            aiThinking = false;
            turnManager.endTurn();
            updateTurnText();
            setStatus("Your turn (White)");
            return;
        }

        int toR = m.toRow, toC = m.toCol;
        int fromR = m.fromRow, fromC = m.fromCol;

        addMoveToHistory(fromR, fromC, toR, toC, movingType, Color.BLACK);
        checkAndPromote(toR, toC, movingType, Color.BLACK);

        animatePieceMove(fromR, fromC, toR, toC, movingNode, () -> {
            refreshBoardFromModel();
            highlightLastMove(fromR, fromC, toR, toC);
            aiThinking = false;

            if (Board.isCheckmate(Color.WHITE)) {
                showGameOver("Stockfish wins. Better luck next time!", false);
                return;
            }
            if (Board.isStalemate(Color.WHITE)) {
                showGameOver("Stalemate! It's a draw.", null);
                return;
            }

            turnManager.endTurn();
            if (clock != null) clock.startWhite();
            try {
                moveManager.recordAfterMove(new Snapshot(Color.WHITE));
                if (snapshotSaver == null) snapshotSaver = new Snapshot(Color.WHITE);
                snapshotSaver.savesnapshopt(Board.getBoard(), Color.WHITE);
            } catch (Exception ignored) {}

            updateTurnText();
            setStatus("Your turn (White)");
        });
    }

    // ─────────────────────────────── Move history ───────────────────────────────

    private void addMoveToHistory(int fr, int fc, int tr, int tc,
                                   piecetype type, Color color) {
        halfMoveCount++;
        String pieceStr = switch (type) {
            case PAWN -> ""; case ROOK -> "R"; case KNIGHT -> "N";
            case BISHOP -> "B"; case QUEEN -> "Q"; case KING -> "K";
        };
        String from = "" + FILES.charAt(fc) + (8 - fr);
        String to   = "" + FILES.charAt(tc) + (8 - tr);
        int moveNum = (halfMoveCount + 1) / 2;
        String entry = color == Color.WHITE
                ? moveNum + ". " + pieceStr + from + "-" + to
                : "    " + pieceStr + from + "-" + to;
        if (moveHistoryView != null) {
            moveHistoryView.getItems().add(entry);
            moveHistoryView.scrollTo(moveHistoryView.getItems().size() - 1);
        }
    }

    // ─────────────────────────────── Captured pieces ───────────────────────────────

    private void refreshCapturedBars() {
        capturedTopLabel.setText(buildCapturedStr(capturedByBlack, Color.WHITE));
        capturedBottomLabel.setText(buildCapturedStr(capturedByWhite, Color.BLACK));
    }

    private String buildCapturedStr(List<piecetype> types, Color capturedColor) {
        StringBuilder sb = new StringBuilder();
        for (piecetype t : types) sb.append(pieceSymbol(t, capturedColor));
        return sb.toString();
    }

    private String pieceSymbol(piecetype t, Color color) {
        if (color == Color.WHITE) return switch (t) {
            case PAWN -> "♙"; case ROOK -> "♖"; case KNIGHT -> "♘";
            case BISHOP -> "♗"; case QUEEN -> "♕"; case KING -> "♔";
        };
        return switch (t) {
            case PAWN -> "♟"; case ROOK -> "♜"; case KNIGHT -> "♞";
            case BISHOP -> "♝"; case QUEEN -> "♛"; case KING -> "♚";
        };
    }

    // ─────────────────────────────── Game over ───────────────────────────────

    /**
     * @param humanWon true = human won, false = AI won, null = draw
     */
    private void showGameOver(String message, Boolean humanWon) {
        if (clock != null) clock.stop();
        boardGrid.setDisable(true);

        if (humanWon != null) {
            if (humanWon) {
                rating += 25;
            } else {
                rating = Math.max(0, rating - 15);
            }
        } else {
            rating += 10;
        }
        Loginpage.updateRating(username, rating);

        ChessDialog.showInfo(stageRef, "Game Over", message + "\n\nYour new rating: " + rating);
    }

    // ─────────────────────────────── Last move highlight ───────────────────────────────

    private void highlightLastMove(int fr, int fc, int tr, int tc) {
        lastFromR = fr; lastFromC = fc; lastToR = tr; lastToC = tc;
        cellRects[fr][fc].setFill(LAST_MOVE_COLOR);
        cellRects[tr][tc].setFill(LAST_MOVE_COLOR);
    }

    // ─────────────────────────────── Animation ───────────────────────────────

    private void animatePieceMove(int fromR, int fromC, int toR, int toC,
                                   Node pieceNode, Runnable onComplete) {
        if (pieceNode == null || animationLayer == null) { onComplete.run(); return; }
        cells[fromR][fromC].getChildren().removeIf(n -> "pieceNode".equals(n.getId()));

        var fromB    = cells[fromR][fromC].localToScene(cells[fromR][fromC].getBoundsInLocal());
        var toB      = cells[toR][toC].localToScene(cells[toR][toC].getBoundsInLocal());
        var overlayB = animationLayer.localToScene(animationLayer.getBoundsInLocal());

        double startX = fromB.getMinX() - overlayB.getMinX();
        double startY = fromB.getMinY() - overlayB.getMinY();
        pieceNode.setLayoutX(startX);
        pieceNode.setLayoutY(startY);
        animationLayer.getChildren().add(pieceNode);

        TranslateTransition tt = new TranslateTransition(Duration.millis(320), pieceNode);
        tt.setByX(toB.getMinX() - overlayB.getMinX() - startX);
        tt.setByY(toB.getMinY() - overlayB.getMinY() - startY);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> { animationLayer.getChildren().remove(pieceNode); onComplete.run(); });
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
                if (moveHistoryView != null && !moveHistoryView.getItems().isEmpty())
                    moveHistoryView.getItems().remove(moveHistoryView.getItems().size() - 1);
                if (halfMoveCount > 0) halfMoveCount--;
                lastFromR = -1; resetColors();
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
        if (turnText != null) turnText.setText("Turn: " + turnManager.getCurrentTurn());
    }

    private void setStatus(String msg) { if (statusText != null) statusText.setText(msg); }
    private void showAlert(String msg) { ChessDialog.showInfo(stageRef, "Chess", msg); }

    private void openDashboard() {
        try {
            DashBoardView dash = new DashBoardView(username, rating, stageRef);
            stageRef.setTitle("Chess - Dashboard");
            stageRef.setScene(new Scene(dash, 900, 700));
        } catch (Exception e) { showAlert("Could not open dashboard."); }
    }

    private void shutdownStockfish() {
        if (clock != null) clock.stop();
        if (stockfish != null) new Thread(() -> stockfish.stop()).start();
    }
}
