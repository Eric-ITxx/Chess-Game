package Frontend;

import gamelogic.*;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class chessboard extends Application {

    private static final int SIZE       = 80;
    private static final int BOARD_SIZE = 8;
    private static final Color SEL_COLOR = Color.rgb(255, 210, 30, 0.9);
    private static final Color LAST_MOVE_COLOR = Color.rgb(255, 220, 50, 0.35);
    private static final String FILES   = "abcdefgh";
    private static final String BG_PATH = chessboard.class.getResource("/Dash.png").toExternalForm();

    // ---------- Backend ----------
    private Board       gameBoard;
    private Player      whitePlayer;
    private Player      blackPlayer;
    private Playerturn  turnManager;
    private MoveManager moveManager;
    private Snapshot    snapshotSaver;

    // ---------- UI ----------
    private GridPane      board;
    private StackPane[][] cells     = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private Rectangle[][] cellRects = new Rectangle[BOARD_SIZE][BOARD_SIZE];
    private StackPane     pauseOverlay;
    private boolean       paused    = false;
    private Text          turnText;
    private boolean       audioOn   = true;

    // ---------- Stage / user ----------
    private Stage  stageRef;
    private String username = "Player";
    private int    rating   = 0;

    // ---------- Selection ----------
    private piece selectedPiece = null;
    private int   selectedRow = -1, selectedCol = -1;

    // ---------- Animation ----------
    private Pane    animationLayer;
    private boolean isAnimating = false;

    // ---------- Clock ----------
    private ChessClock clock;

    // ---------- Last move highlight ----------
    private int lastFromR = -1, lastFromC = -1, lastToR = -1, lastToC = -1;

    // ---------- Move history ----------
    private final List<String> moveHistory = new ArrayList<>();
    private ListView<String>   moveHistoryView;
    private int halfMoveCount = 0;

    // ---------- Captured pieces ----------
    private final List<piecetype> capturedByWhite = new ArrayList<>(); // black pieces white captured
    private final List<piecetype> capturedByBlack = new ArrayList<>(); // white pieces black captured
    private Label capturedTopLabel;    // top bar: what black has captured (white pieces)
    private Label capturedBottomLabel; // bottom bar: what white has captured (black pieces)

    // ---------- Images ----------
    private Image whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteKing;
    private Image blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackKing;

    // ---------- Public setters (called from Dashboard) ----------
    public void setUsername(String u) { this.username = u; }
    public void setRating(int r)      { this.rating = r;   }

    @Override
    public void start(Stage stage) {
        this.stageRef = stage;

        gameBoard     = new Board();
        whitePlayer   = new Player(gamelogic.Color.WHITE, username);
        blackPlayer   = new Player(gamelogic.Color.BLACK, "Player 2");
        turnManager   = new Playerturn(gamelogic.Color.WHITE);

        askLoadOrNewGame();
        askTimeControl();

        moveManager = new MoveManager();
        try { moveManager.recordInitial(new Snapshot(turnManager.getCurrentTurn())); }
        catch (Exception ignored) {}

        buildBoard();
        loadPieceImages();
        refreshBoardFromModel();

        pauseOverlay   = buildPauseOverlay();
        VBox buttons   = buildRightButtons();
        animationLayer = new Pane();
        animationLayer.setMouseTransparent(true);

        // Captured-pieces bars
        capturedTopLabel    = buildCapturedLabel();
        capturedBottomLabel = buildCapturedLabel();

        VBox boardArea = new VBox(4,
                capturedTopLabel,
                new StackPane(board, pauseOverlay, animationLayer),
                capturedBottomLabel);
        boardArea.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(boardArea);
        root.setRight(buttons);
        root.setStyle(
            "-fx-background-image: url('" + BG_PATH + "');" +
            "-fx-background-size: cover;" +
            "-fx-background-position: center;" +
            "-fx-background-repeat: no-repeat;"
        );

        Scene scene = new Scene(root, 1100, 780);
        stage.setTitle("Chess  PvsP");
        stage.setScene(scene);
        stage.show();

        if (clock != null) clock.startWhite();
        updateTurnText();
    }

    // ===================== TIME CONTROL =====================
    private void askTimeControl() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Time Control", "Choose a time control:",
                "Bullet  1 min", "Rapid  10 min", "Classical  30 min", "No Limit");
        long secs = switch (choice) {
            case "Bullet  1 min"     -> 60L;
            case "Rapid  10 min"     -> 600L;
            case "Classical  30 min" -> 1800L;
            default                  -> 0L;
        };
        if (secs > 0) {
            clock = new ChessClock(secs,
                () -> handleTimeUp("Black wins — White ran out of time!"),
                () -> handleTimeUp("White wins — Black ran out of time!")
            );
        }
    }

    private void handleTimeUp(String msg) {
        board.setDisable(true);
        ChessDialog.showInfo(stageRef, "Time's Up!", msg);
    }

    // ===================== LOAD OR NEW =====================
    private void askLoadOrNewGame() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Player vs Player", "Continue saved game or start fresh?",
                "New Game", "Load Saved");
        if ("Load Saved".equals(choice)) {
            try {
                Board.displaysboard();
                gamelogic.Color saved = Snapshot.getColor();
                if (saved != null) turnManager = new Playerturn(saved);
            } catch (Exception e) {
                ChessDialog.showInfo(stageRef, "Chess", "Could not load save. Starting new.");
            }
        }
    }

    // ===================== BOARD UI =====================
    private void buildBoard() {
        board = new GridPane();
        board.setPadding(new Insets(10, 20, 20, 20));
        board.setAlignment(Pos.CENTER);

        Font coordFont = Font.font(14);
        for (int c = 0; c < BOARD_SIZE; c++) {
            Label file = new Label(String.valueOf(FILES.charAt(c)));
            file.setMinWidth(SIZE);  file.setAlignment(Pos.CENTER);
            file.setTextFill(Color.WHITE); file.setFont(coordFont);
            file.setTranslateY(-8);
            board.add(file, c + 1, 0);
        }
        for (int r = 0; r < BOARD_SIZE; r++) {
            Label rank = new Label(String.valueOf(8 - r));
            rank.setMinHeight(SIZE); rank.setAlignment(Pos.CENTER);
            rank.setTextFill(Color.WHITE); rank.setFont(coordFont);
            rank.setTranslateY(-6);
            board.add(rank, 0, r + 1);
        }

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Rectangle rect = new Rectangle(SIZE, SIZE);
                rect.setFill((r + c) % 2 == 0 ? GameSettings.getLightSq() : GameSettings.getDarkSq());
                rect.setStroke(Color.rgb(15, 15, 15));

                StackPane cell = new StackPane(rect);
                int rr = r, cc = c;
                cell.setOnMouseClicked(e -> handleHumanClick(rr, cc));

                cells[r][c]     = cell;
                cellRects[r][c] = rect;
                board.add(cell, c + 1, r + 1);
            }
        }
    }

    private Label buildCapturedLabel() {
        Label l = new Label();
        l.setStyle("-fx-font-size: 22; -fx-text-fill: white; -fx-padding: 2 20;");
        return l;
    }

    // ===================== RIGHT PANEL =====================
    private VBox buildRightButtons() {

        turnText = new Text();
        turnText.setFill(Color.WHITE);
        turnText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Text blackClock = (clock != null) ? clock.getBlackLabel() : new Text("Black  --:--");
        Text whiteClock = (clock != null) ? clock.getWhiteLabel() : new Text("White  --:--");
        blackClock.setFill(Color.WHITE); blackClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");
        whiteClock.setFill(Color.WHITE); whiteClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        // Move history
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
        Button audio  = styledButton("Audio: ON");
        Button back   = styledButton("Back");
        Button exit   = styledButton("Exit");

        pause .setOnAction(e -> { paused = true;  pauseOverlay.setVisible(true);  if (clock != null) clock.pause();  });
        resume.setOnAction(e -> { paused = false; pauseOverlay.setVisible(false); if (clock != null) clock.resume(); });
        undo  .setOnAction(e -> doUndo());
        redo  .setOnAction(e -> doRedo());
        save  .setOnAction(e -> {
            try {
                snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn());
                showAlert("Game saved.");
            } catch (Exception ex) { showAlert("Save failed."); }
        });
        audio.setOnAction(e -> {
            audioOn = !audioOn;
            audio.setText(audioOn ? "Audio: ON" : "Audio: OFF");
        });
        back.setOnAction(e -> { if (clock != null) clock.stop(); openDashboard(); });
        exit.setOnAction(e -> { if (stageRef != null) stageRef.close(); });

        VBox box = new VBox(10,
                blackClock, turnText, whiteClock,
                historyTitle, moveHistoryView,
                pause, resume, undo, redo, save, audio, back, exit);
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
        Rectangle bg   = new Rectangle(900, 740, Color.rgb(0, 0, 0, 0.55));
        Text      text = new Text("PAUSED");
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 52; -fx-font-weight: bold;");
        StackPane overlay = new StackPane(bg, text);
        overlay.setVisible(false);
        return overlay;
    }

    // ===================== IMAGES =====================
    private void loadPieceImages() {
        whitePawn   = loadFileImage("/Pawn.jpeg");
        whiteRook   = loadFileImage("/white rook.jpeg");
        whiteKnight = loadFileImage("/white horse.jpeg");
        whiteBishop = loadFileImage("/white bishop.jpeg");
        whiteQueen  = loadFileImage("/white queen.jpeg");
        whiteKing   = loadFileImage("/white king.jpeg");
        blackPawn   = loadFileImage("/black pawn.jpeg");
        blackRook   = loadFileImage("/black rook.jpeg");
        blackKnight = loadFileImage("/black horse.jpeg");
        blackBishop = loadFileImage("/black bishop.jpeg");
        blackQueen  = loadFileImage("/black queen.jpeg");
        blackKing   = loadFileImage("/black king.jpeg");
    }

    private Image loadFileImage(String path) {
        try { var in = getClass().getResourceAsStream(path); return in != null ? new Image(in) : null; }
        catch (Exception e) { return null; }
    }

    // ===================== DRAW =====================
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

    // ===================== HUMAN MOVE (PvsP) =====================
    private void handleHumanClick(int r, int c) {
        if (paused || isAnimating) return;

        piece[][] model  = Board.getBoard();
        piece    clicked = model[r][c];
        resetColors();

        // First click — select piece
        if (selectedPiece == null) {
            if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
                selectedPiece = clicked;
                selectedRow   = r;
                selectedCol   = c;
                cellRects[r][c].setFill(SEL_COLOR);
                showLegalDots(clicked, model);
            }
            return;
        }

        // Clicked own piece — re-select
        if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
            selectedPiece = null;
            handleHumanClick(r, c);
            return;
        }

        // Capture tracking — grab the victim BEFORE move
        piece victim = model[r][c];

        int       fromR        = selectedPiece.getRow();
        int       fromC        = selectedPiece.getCol();
        piecetype movingType   = selectedPiece.getType();
        gamelogic.Color movingColor = selectedPiece.getColor();

        String  target = piece.rcToAlgebraic(r, c);
        boolean moved  = selectedPiece.move(target);
        selectedPiece  = null;

        if (moved) {
            // Track capture
            if (victim != null) {
                if (movingColor == gamelogic.Color.WHITE) capturedByWhite.add(victim.getType());
                else                                      capturedByBlack.add(victim.getType());
                refreshCapturedBars();
            }

            // Record move history entry
            addMoveToHistory(fromR, fromC, r, c, movingType, movingColor);

            // Pawn promotion
            checkAndPromote(r, c, movingType, movingColor);

            Node animNode = PieceRenderer.createPiece(movingType, movingColor, SIZE);
            animatePieceMove(fromR, fromC, r, c, animNode, () -> {
                refreshBoardFromModel();
                highlightLastMove(fromR, fromC, r, c);

                gamelogic.Color opponent = (movingColor == gamelogic.Color.WHITE)
                        ? gamelogic.Color.BLACK : gamelogic.Color.WHITE;

                if (Board.isCheckmate(opponent)) {
                    showGameOver(movingColor + " wins by checkmate!", movingColor);
                    return;
                }
                if (Board.isStalemate(opponent)) {
                    showGameOver("Stalemate! It's a draw.", null);
                    return;
                }

                turnManager.endTurn();
                try {
                    moveManager.recordAfterMove(new Snapshot(turnManager.getCurrentTurn()));
                    if (snapshotSaver == null) snapshotSaver = new Snapshot(turnManager.getCurrentTurn());
                    snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn());
                } catch (Exception ignored) {}

                if (clock != null) {
                    if (turnManager.getCurrentTurn() == gamelogic.Color.WHITE) clock.startWhite();
                    else                                                        clock.startBlack();
                }
                updateTurnText();
            });
        }
    }

    // ===================== LEGAL MOVE DOTS =====================
    private void showLegalDots(piece p, piece[][] model) {
        for (String mv : p.getPossibleMoves(model)) {
            if (Board.isMoveLegal(p, mv)) {
                int[] rc       = piece.algebraicToRC(mv);
                boolean isCapt = model[rc[0]][rc[1]] != null;
                Circle dot     = new Circle(isCapt ? 34 : 11);
                if (isCapt) {
                    dot.setFill(Color.TRANSPARENT);
                    dot.setStroke(Color.rgb(80, 220, 80, 0.75));
                    dot.setStrokeWidth(4);
                } else {
                    dot.setFill(Color.rgb(80, 220, 80, 0.55));
                }
                dot.setMouseTransparent(true);
                dot.setId("legalDot");
                cells[rc[0]][rc[1]].getChildren().add(dot);
            }
        }
    }

    // ===================== PAWN PROMOTION =====================
    private void checkAndPromote(int r, int c, piecetype type, gamelogic.Color color) {
        if (type != piecetype.PAWN) return;
        boolean isPromotion = (color == gamelogic.Color.WHITE && r == 0)
                           || (color == gamelogic.Color.BLACK && r == 7);
        if (!isPromotion) return;

        String choice = ChessDialog.showConfirm(stageRef, "Pawn Promotion!",
                "Choose promotion piece:",
                "Queen", "Rook", "Bishop", "Knight");
        piecetype newType = switch (choice) {
            case "Rook"   -> piecetype.ROOK;
            case "Bishop" -> piecetype.BISHOP;
            case "Knight" -> piecetype.KNIGHT;
            default       -> piecetype.QUEEN;
        };
        Board.promotePawn(r, c, newType);
    }

    // ===================== MOVE HISTORY =====================
    private void addMoveToHistory(int fr, int fc, int tr, int tc,
                                   piecetype type, gamelogic.Color color) {
        halfMoveCount++;
        String pieceStr = switch (type) {
            case PAWN -> ""; case ROOK -> "R"; case KNIGHT -> "N";
            case BISHOP -> "B"; case QUEEN -> "Q"; case KING -> "K";
        };
        String from = "" + FILES.charAt(fc) + (8 - fr);
        String to   = "" + FILES.charAt(tc) + (8 - tr);
        int moveNum = (halfMoveCount + 1) / 2;
        String entry = color == gamelogic.Color.WHITE
                ? moveNum + ". " + pieceStr + from + "-" + to
                : "    " + pieceStr + from + "-" + to;
        if (moveHistoryView != null) {
            moveHistoryView.getItems().add(entry);
            moveHistoryView.scrollTo(moveHistoryView.getItems().size() - 1);
        }
    }

    // ===================== CAPTURED PIECES =====================
    private void refreshCapturedBars() {
        // Top = what black captured (white pieces)
        capturedTopLabel.setText(buildCapturedStr(capturedByBlack, gamelogic.Color.WHITE));
        // Bottom = what white captured (black pieces)
        capturedBottomLabel.setText(buildCapturedStr(capturedByWhite, gamelogic.Color.BLACK));
    }

    private String buildCapturedStr(List<piecetype> types, gamelogic.Color capturedColor) {
        StringBuilder sb = new StringBuilder();
        for (piecetype t : types) sb.append(pieceSymbol(t, capturedColor));
        return sb.toString();
    }

    private String pieceSymbol(piecetype t, gamelogic.Color color) {
        if (color == gamelogic.Color.WHITE) return switch (t) {
            case PAWN -> "♙"; case ROOK -> "♖"; case KNIGHT -> "♘";
            case BISHOP -> "♗"; case QUEEN -> "♕"; case KING -> "♔";
        };
        return switch (t) {
            case PAWN -> "♟"; case ROOK -> "♜"; case KNIGHT -> "♞";
            case BISHOP -> "♝"; case QUEEN -> "♛"; case KING -> "♚";
        };
    }

    // ===================== GAME OVER =====================
    private void showGameOver(String message, gamelogic.Color winner) {
        if (clock != null) clock.stop();
        board.setDisable(true);

        // Update ELO for the logged-in player (white)
        if (winner == gamelogic.Color.WHITE) {
            rating += 25;
            Loginpage.updateRating(username, rating);
        } else if (winner == gamelogic.Color.BLACK) {
            rating = Math.max(0, rating - 15);
            Loginpage.updateRating(username, rating);
        } else {
            rating += 10; // draw
            Loginpage.updateRating(username, rating);
        }

        String fullMsg = message + "\n\nYour new rating: " + rating;
        ChessDialog.showInfo(stageRef, "Game Over", fullMsg);
    }

    // ===================== LAST MOVE HIGHLIGHT =====================
    private void highlightLastMove(int fr, int fc, int tr, int tc) {
        lastFromR = fr; lastFromC = fc; lastToR = tr; lastToC = tc;
        cellRects[fr][fc].setFill(LAST_MOVE_COLOR);
        cellRects[tr][tc].setFill(LAST_MOVE_COLOR);
    }

    // ===================== ANIMATION =====================
    private void animatePieceMove(int fromR, int fromC, int toR, int toC,
                                   Node pieceNode, Runnable onComplete) {
        if (pieceNode == null) { onComplete.run(); return; }
        isAnimating = true;
        cells[fromR][fromC].getChildren().removeIf(n -> "pieceNode".equals(n.getId()));

        var fromB    = cells[fromR][fromC].localToScene(cells[fromR][fromC].getBoundsInLocal());
        var toB      = cells[toR][toC].localToScene(cells[toR][toC].getBoundsInLocal());
        var overlayB = animationLayer.localToScene(animationLayer.getBoundsInLocal());

        double startX = fromB.getMinX() - overlayB.getMinX();
        double startY = fromB.getMinY() - overlayB.getMinY();
        pieceNode.setLayoutX(startX);
        pieceNode.setLayoutY(startY);
        animationLayer.getChildren().add(pieceNode);

        TranslateTransition tt = new TranslateTransition(Duration.millis(280), pieceNode);
        tt.setByX(toB.getMinX() - overlayB.getMinX() - startX);
        tt.setByY(toB.getMinY() - overlayB.getMinY() - startY);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> { animationLayer.getChildren().remove(pieceNode); isAnimating = false; onComplete.run(); });
        tt.play();
    }

    // ===================== UNDO / REDO =====================
    private void doUndo() {
        if (paused) return;
        if (moveManager == null || !moveManager.canUndo()) return;
        try {
            Snapshot prev = moveManager.undo(new Snapshot(turnManager.getCurrentTurn()));
            if (prev != null) {
                Board.displayundoredo(prev.getBoardstring());
                if (prev.getSnapshotColor() != null) turnManager = new Playerturn(prev.getSnapshotColor());
                refreshBoardFromModel();
                updateTurnText();
                // Undo last move history entry
                if (moveHistoryView != null && !moveHistoryView.getItems().isEmpty())
                    moveHistoryView.getItems().remove(moveHistoryView.getItems().size() - 1);
                if (halfMoveCount > 0) halfMoveCount--;
                lastFromR = -1; resetColors();
            }
        } catch (Exception ignored) {}
    }

    private void doRedo() {
        if (paused) return;
        if (moveManager == null || !moveManager.canRedo()) return;
        try {
            Snapshot next = moveManager.redo(new Snapshot(turnManager.getCurrentTurn()));
            if (next != null) {
                Board.displayundoredo(next.getBoardstring());
                if (next.getSnapshotColor() != null) turnManager = new Playerturn(next.getSnapshotColor());
                refreshBoardFromModel();
                updateTurnText();
            }
        } catch (Exception ignored) {}
    }

    // ===================== HELPERS =====================
    private void resetColors() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                // base color
                Color base = (r + c) % 2 == 0 ? GameSettings.getLightSq() : GameSettings.getDarkSq();
                // last move tint takes priority
                if ((r == lastFromR && c == lastFromC) || (r == lastToR && c == lastToC))
                    cellRects[r][c].setFill(LAST_MOVE_COLOR);
                else
                    cellRects[r][c].setFill(base);
                cells[r][c].getChildren().removeIf(n -> "legalDot".equals(n.getId()));
            }
    }

    private void updateTurnText() {
        if (turnText != null) turnText.setText("Turn: " + turnManager.getCurrentTurn());
    }

    private void showAlert(String msg) { ChessDialog.showInfo(stageRef, "Chess", msg); }

    private void openDashboard() {
        try {
            DashBoardView dash = new DashBoardView(username, rating, stageRef);
            stageRef.setTitle("Chess - Dashboard");
            stageRef.setScene(new Scene(dash, 900, 700));
        } catch (Exception e) { showAlert("Dashboard open failed."); }
    }

    public static void main(String[] args) { launch(args); }
}
