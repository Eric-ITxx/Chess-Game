package Frontend;

import gamelogic.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.util.List;

public class chessboard extends Application {

    private static final int SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final String FILES = "abcdefgh";

    private static final String BG_PATH = "file:src/main/resources/Dash.png";

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

        BorderPane root = new BorderPane();
        root.setCenter(new StackPane(board, pauseOverlay));
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

        updateTurnText();
    }

    // ================= START MENU =================
    private void askLoadOrNewGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Chess PvsP");
        alert.setHeaderText("Start Game");
        alert.setContentText("Choose an option:");

        ButtonType newGame = new ButtonType("New Game");
        ButtonType loadGame = new ButtonType("Load Saved");
        alert.getButtonTypes().setAll(newGame, loadGame);

        alert.showAndWait().ifPresent(btn -> {
            if (btn == loadGame) {
                try {
                    Board.displaysboard();
                    gamelogic.Color saved = Snapshot.getColor();
                    if (saved != null) turnManager = new Playerturn(saved);
                } catch (Exception e) {
                    showAlert("Could not load save. Starting new.");
                }
            }
        });
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
                rect.setFill((r + c) % 2 == 0 ? Color.LIGHTBLUE : Color.BEIGE);
                rect.setStroke(Color.GRAY);

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
        });

        resume.setOnAction(e -> {
            paused = false;
            pauseOverlay.setVisible(false);
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


        back.setOnAction(e -> openDashboard());


        exit.setOnAction(e -> {
            if (stageRef != null) stageRef.close();
        });

        VBox box = new VBox(12,
                turnText,
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
        whitePawn   = loadFileImage("file:src/main/resources/Pawn.jpeg");
        whiteRook   = loadFileImage("file:src/main/resources/white rook.jpeg");
        whiteKnight = loadFileImage("file:src/main/resources/white horse.jpeg");
        whiteBishop = loadFileImage("file:src/main/resources/white bishop.jpeg");
        whiteQueen  = loadFileImage("file:src/main/resources/white queen.jpeg");
        whiteKing   = loadFileImage("file:src/main/resources/white king.jpeg");

        blackPawn   = loadFileImage("file:src/main/resources/black pawn.jpeg");
        blackRook   = loadFileImage("file:src/main/resources/black rook.jpeg");
        blackKnight = loadFileImage("file:src/main/resources/black horse.jpeg");
        blackBishop = loadFileImage("file:src/main/resources/black bishop.jpeg");
        blackQueen  = loadFileImage("file:src/main/resources/black queen.jpeg");
        blackKing   = loadFileImage("file:src/main/resources/black king.jpeg");
    }

    private Image loadFileImage(String filePath) {
        try {
            return new Image(filePath);
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
                cell.getChildren().removeIf(n -> (n instanceof ImageView) || (n instanceof Label));

        piece[][] model = Board.getBoard();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                piece p = model[r][c];
                if (p != null) {
                    Image img = getImageForPiece(p);
                    if (img != null) {
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(SIZE * 0.85);
                        iv.setFitHeight(SIZE * 0.85);
                        cells[r][c].getChildren().add(iv);
                    }
//                    else {
//                        Label sym = new Label(String.valueOf(p.getSymbol()));
//                        sym.setTextFill(Color.BLACK);
//                        sym.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");
//                        cells[r][c].getChildren().add(sym);
//                    }
                }
            }
        }

//        updateTurnText();
    }

    // ================= HUMAN MOVE (PvsP) =================
    private void handleHumanClick(int r, int c) {

        if (paused) return;

        piece[][] model = Board.getBoard();
        piece clicked = model[r][c];

        resetColors();

        if (selectedPiece == null) {
            if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
                selectedPiece = clicked;
                selectedRow = r;
                selectedCol = c;

                cellRects[r][c].setFill(Color.GOLD);

                List<String> moves = clicked.getPossibleMoves(model);
                for (String mv : moves) {
                    int[] rc = piece.algebraicToRC(mv);
                    cellRects[rc[0]][rc[1]].setFill(Color.LIGHTGREEN);
                }
            }
            return;
        }

        if (clicked != null && clicked.getColor() == turnManager.getCurrentTurn()) {
            selectedPiece = null;
            handleHumanClick(r, c);
            return;
        }

        String target = piece.rcToAlgebraic(r, c);
        boolean moved = selectedPiece.move(target);

        selectedPiece = null;

        if (moved) {
            refreshBoardFromModel();

            gamelogic.Color opponent = (turnManager.getCurrentTurn() == gamelogic.Color.WHITE)
                    ? gamelogic.Color.BLACK : gamelogic.Color.WHITE;

            if (Board.isCheckmate(opponent)) {
                showAlert(turnManager.getCurrentTurn() + " wins by checkmate!");
                board.setDisable(true);
                return;
            }

            turnManager.endTurn();

            try {
                moveManager.recordAfterMove(new Snapshot(turnManager.getCurrentTurn()));
                if (snapshotSaver == null) snapshotSaver = new Snapshot(turnManager.getCurrentTurn());
                snapshotSaver.savesnapshopt(Board.getBoard(), turnManager.getCurrentTurn());
            } catch (Exception ignored) {
                System.out.println(ignored.getMessage());
            }

            updateTurnText();
        }
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
            for (int c = 0; c < 8; c++)
                cellRects[r][c].setFill((r + c) % 2 == 0 ? Color.LIGHTBLUE : Color.BEIGE);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chess");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
