package Frontend;

import gamelogic.*;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Bounds;
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

import java.util.List;

public class chessboard extends Application {

    private static final int SIZE = 80;
    private static final int BOARD_SIZE = 8;

    // Board color scheme — silverish black
    private static final Color LIGHT_SQ  = Color.rgb(180, 188, 196); // steel silver
    private static final Color DARK_SQ   = Color.rgb(34, 34, 34);    // near black
    private static final Color SEL_COLOR = Color.rgb(255, 210, 30, 0.9); // gold selection
    private static final String FILES = "abcdefgh";

    private static final String BG_PATH = chessboard.class.getResource("/Dash.png").toExternalForm();

    // ---------- Backend ----------
    private Board gameBoard;
    private Player whitePlayer;
    private Player blackPlayer;
    private Playerturn turnManager;

    private MoveManager moveManager;
    private Snapshot snapshotSaver;

    // ---------- UI ----------
    private GridPane board;
    private StackPane[][] cells = new StackPane[BOARD_SIZE][BOARD_SIZE];
    private Rectangle[][] cellRects = new Rectangle[BOARD_SIZE][BOARD_SIZE];

    private StackPane pauseOverlay;
    private boolean paused = false;

    private Text turnText;


    private boolean audioOn = true;

    // needed for back-to-dashboard
    private Stage stageRef;
    private String username = "Player";
    private int rating = 0;

    // Human selection state
    private piece selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;

    // Animation
    private Pane animationLayer;
    private boolean isAnimating = false;

    // Clock
    private ChessClock clock;


    // Images
    private Image whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteKing;
    private Image blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackKing;

    @Override
    public void start(Stage stage) {

        this.stageRef = stage;

        gameBoard = new Board();
        whitePlayer = new Player(gamelogic.Color.WHITE, "Player 1");
        blackPlayer = new Player(gamelogic.Color.BLACK, "Player 2");
        turnManager = new Playerturn(gamelogic.Color.WHITE);

        askLoadOrNewGame();
        askTimeControl();

        moveManager = new MoveManager();
        try {
            moveManager.recordInitial(new Snapshot(turnManager.getCurrentTurn()));
        } catch (Exception ignored) {
            System.out.println("Move failed");
        }

        buildBoard();
        loadPieceImages();
        refreshBoardFromModel();

        pauseOverlay = buildPauseOverlay();
        VBox buttons = buildRightButtons();

        animationLayer = new Pane();
        animationLayer.setMouseTransparent(true);

        BorderPane root = new BorderPane();
        root.setCenter(new StackPane(board, pauseOverlay, animationLayer));
        root.setRight(buttons);

        root.setStyle(
                "-fx-background-image: url('" + BG_PATH + "');" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center;" +
                        "-fx-background-repeat: no-repeat;"
        );

        Scene scene = new Scene(root, 1100, 780);
        stage.setTitle("Chess PvsP");
        stage.setScene(scene);

        stage.show();

        // Start clock after UI is shown
        if (clock != null) clock.startWhite();

        updateTurnText();
    }

    // ================= TIME CONTROL =================
    private long askTimeControl() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Time Control",
                "Choose a time control for this game:",
                "Bullet  1 min", "Rapid  10 min", "Classical  30 min", "No Limit");
        long secs = switch (choice) {
            case "Bullet  1 min"     -> 60L;
            case "Rapid  10 min"     -> 600L;
            case "Classical  30 min" -> 1800L;
            default                  -> 0L;
        };
        if (secs > 0) {
            clock = new ChessClock(secs,
                () -> { ChessDialog.showInfo(stageRef, "Time's Up!", "Black wins — White ran out of time!"); board.setDisable(true); },
                () -> { ChessDialog.showInfo(stageRef, "Time's Up!", "White wins — Black ran out of time!"); board.setDisable(true); }
            );
        }
        return secs;
    }

    // ================= START MENU =================
    private void askLoadOrNewGame() {
        String choice = ChessDialog.showConfirm(stageRef,
                "Player vs Player",
                "Would you like to continue a saved game or start fresh?",
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

    // ================= BOARD UI =================
    private void buildBoard() {

        board = new GridPane();
        board.setPadding(new Insets(10, 20, 20, 20));
        board.setAlignment(Pos.CENTER);


        Font coordFont = Font.font(14);

        // Header row (a-h)
        for (int c = 0; c < BOARD_SIZE; c++) {
            Label file = new Label(String.valueOf(FILES.charAt(c)));
            file.setMinWidth(SIZE);
            file.setAlignment(Pos.CENTER);
            file.setTextFill(Color.WHITE);
            file.setFont(coordFont);
            file.setTranslateY(-8);
            board.add(file, c + 1, 0);
        }

        // Side column (8-1)
        for (int r = 0; r < BOARD_SIZE; r++) {
            Label rank = new Label(String.valueOf(8 - r));
            rank.setMinHeight(SIZE);
            rank.setAlignment(Pos.CENTER);
            rank.setTextFill(Color.WHITE);
            rank.setFont(coordFont);
            rank.setTranslateY(-6);
            board.add(rank, 0, r + 1);
        }

        // Cells
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {

                Rectangle rect = new Rectangle(SIZE, SIZE);
                rect.setFill((r + c) % 2 == 0 ? LIGHT_SQ : DARK_SQ);
                rect.setStroke(Color.rgb(15, 15, 15));

                StackPane cell = new StackPane(rect);
                int rr = r, cc = c;
                cell.setOnMouseClicked(e -> handleHumanClick(rr, cc));

                cells[r][c] = cell;
                cellRects[r][c] = rect;

                board.add(cell, c + 1, r + 1);
            }
        }
    }

    private VBox buildRightButtons() {

        turnText = new Text();
        turnText.setFill(Color.WHITE);
        turnText.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        // Clock labels (placeholders if no clock selected)
        Text blackClock = (clock != null) ? clock.getBlackLabel() : new Text("Black  --:--");
        Text whiteClock = (clock != null) ? clock.getWhiteLabel() : new Text("White  --:--");
        blackClock.setFill(Color.WHITE);
        whiteClock.setFill(Color.WHITE);
        blackClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");
        whiteClock.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        Button pause  = styledButton("Pause");
        Button resume = styledButton("Resume");
        Button undo   = styledButton("Undo");
        Button redo   = styledButton("Redo");
        Button save   = styledButton("Save");


        Button audio  = styledButton("Audio: ON");
        Button back   = styledButton("Back");
        Button exit   = styledButton("Exit");

        pause.setOnAction(e -> {
            paused = true;
            pauseOverlay.setVisible(true);
            if (clock != null) clock.pause();
        });

        resume.setOnAction(e -> {
            paused = false;
            pauseOverlay.setVisible(false);
            if (clock != null) clock.resume();
        });

        undo.setOnAction(e -> doUndo());
        redo.setOnAction(e -> doRedo());

        save.setOnAction(e -> {
            try {
                snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn());
                showAlert("Game saved.");
            } catch (Exception ex) {
                showAlert("Save failed.");
            }
        });


        audio.setOnAction(e -> {
            audioOn = !audioOn;
            audio.setText(audioOn ? "Audio: ON" : "Audio: OFF");
        });


        back.setOnAction(e -> { if (clock != null) clock.stop(); openDashboard(); });


        exit.setOnAction(e -> {
            if (stageRef != null) stageRef.close();
        });

        VBox box = new VBox(12,
                blackClock,
                turnText,
                whiteClock,
                pause, resume,
                undo, redo,
                save,
                audio, back, exit
        );
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
                "-fx-font-size: 16;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(60,60,60,0.85);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;"
        );
        return b;
    }

    private StackPane buildPauseOverlay() {
        Rectangle bg = new Rectangle(900, 740, Color.rgb(0, 0, 0, 0.55));
        Text text = new Text("PAUSED");
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 52; -fx-font-weight: bold;");

        StackPane overlay = new StackPane(bg, text);
        overlay.setVisible(false);
        return overlay;
    }


    private void openDashboard() {
        try {
            DashBoardView dash = new DashBoardView(username, rating, stageRef);
            Scene dashboardScene = new Scene(dash, 900, 700);
            stageRef.setTitle("Chess - Dashboard");
            stageRef.setScene(dashboardScene);
        } catch (Exception e) {
            showAlert("Dashboard open failed. Check DashBoardView constructor.");
        }
    }

    // ================= IMAGES =================
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

    private Image loadFileImage(String classpathPath) {
        try {
            var in = getClass().getResourceAsStream(classpathPath);
            return in != null ? new Image(in) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Image getImageForPiece(piece p) {
        if (p.getColor() == gamelogic.Color.WHITE) {
            return switch (p.getType()) {
                case PAWN -> whitePawn;
                case ROOK -> whiteRook;
                case KNIGHT -> whiteKnight;
                case BISHOP -> whiteBishop;
                case QUEEN -> whiteQueen;
                case KING -> whiteKing;
            };
        } else {
            return switch (p.getType()) {
                case PAWN -> blackPawn;
                case ROOK -> blackRook;
                case KNIGHT -> blackKnight;
                case BISHOP -> blackBishop;
                case QUEEN -> blackQueen;
                case KING -> blackKing;
            };
        }
    }

    // ================= DRAW =================
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

    // ================= HUMAN MOVE (PvsP) =================
    private void handleHumanClick(int r, int c) {

        if (paused || isAnimating) return;

        piece[][] model = Board.getBoard();
        piece clicked = model[r][c];

        resetColors();

        if (selectedPiece == null) {
            if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
                selectedPiece = clicked;
                selectedRow = r;
                selectedCol = c;

                cellRects[r][c].setFill(SEL_COLOR);

                List<String> moves = clicked.getPossibleMoves(model);
                for (String mv : moves) {
                    if (Board.isMoveLegal(clicked, mv)) {
                        int[] rc = piece.algebraicToRC(mv);
                        // Show a dot for empty squares, a ring for captures
                        boolean isCapture = model[rc[0]][rc[1]] != null;
                        Circle dot = new Circle(isCapture ? 34 : 11);
                        if (isCapture) {
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
            return;
        }

        if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
            selectedPiece = null;
            handleHumanClick(r, c);
            return;
        }

        // Save position + piece info BEFORE applying the move
        int fromR = selectedPiece.getRow();
        int fromC = selectedPiece.getCol();
        piecetype movingType  = selectedPiece.getType();
        gamelogic.Color movingColor = selectedPiece.getColor();

        String target = piece.rcToAlgebraic(r, c);
        boolean moved = selectedPiece.move(target);
        selectedPiece = null;

        if (moved) {
            Node animNode = PieceRenderer.createPiece(movingType, movingColor, SIZE);
            animatePieceMove(fromR, fromC, r, c, animNode, () -> {
                refreshBoardFromModel();

                gamelogic.Color opponent = (turnManager.getCurrentTurn() == gamelogic.Color.WHITE)
                        ? gamelogic.Color.BLACK : gamelogic.Color.WHITE;

                if (Board.isCheckmate(opponent)) {
                    if (clock != null) clock.stop();
                    ChessDialog.showInfo(stageRef,
                            "Checkmate!",
                            turnManager.getCurrentTurn() + " wins by checkmate!");
                    board.setDisable(true);
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
                    else clock.startBlack();
                }
                updateTurnText();
            });
        }
    }

    // ================= ANIMATION =================
    private void animatePieceMove(int fromR, int fromC, int toR, int toC,
                                   Node pieceNode, Runnable onComplete) {
        if (pieceNode == null) { onComplete.run(); return; }
        isAnimating = true;

        // Clear piece from source cell visually
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

        TranslateTransition tt = new TranslateTransition(Duration.millis(280), pieceNode);
        tt.setByX(endX - startX);
        tt.setByY(endY - startY);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> {
            animationLayer.getChildren().remove(pieceNode);
            isAnimating = false;
            onComplete.run();
        });
        tt.play();
    }

    // ================= UNDO / REDO =================
    private void doUndo() {
        if (paused) return;
        if (moveManager == null || !moveManager.canUndo()) return;

        try {
            Snapshot prev = moveManager.undo(new Snapshot(turnManager.getCurrentTurn()));
            if (prev != null) {
                Board.displayundoredo(prev.getBoardstring());
                if (prev.getSnapshotColor() != null) turnManager = new Playerturn(prev.getSnapshotColor());
                refreshBoardFromModel();
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
            }
        } catch (Exception ignored) {}
    }

    // ================= HELPERS =================
    private void updateTurnText() {
        if (turnText != null) turnText.setText("Turn: " + turnManager.getCurrentTurn());
    }

    private void resetColors() {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                cellRects[r][c].setFill((r + c) % 2 == 0 ? LIGHT_SQ : DARK_SQ);
                cells[r][c].getChildren().removeIf(n -> "legalDot".equals(n.getId()));
            }
    }

    private void showAlert(String msg) {
        ChessDialog.showInfo(stageRef, "Chess", msg);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
