package Frontend;

import Network.Move;
import gamelogic.*;
import Network.GUIclient;
import Network.GUIserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class chesslan extends Application {
    //userclick piece select move execute validation update board switch turn

    private static final int SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private static final String FILES = "abcdefgh";

    // Backend
    private Board gameBoard;
    private Player whitePlayer;
    private Player blackPlayer;
    private Playerturn turnManager;
    private boolean vsComputer = false;
    private ComputerPlayer ai;

    // UI
    private GridPane board;
    private piece selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private final List<Circle> moveHighlights = new ArrayList<>();

    // Images
    private Image whitePawn, whiteRook, whiteKnight, whiteBishop, whiteQueen, whiteKing;
    private Image blackPawn, blackRook, blackKnight, blackBishop, blackQueen, blackKing;

    // LAN
    private boolean isLAN = false;
    private boolean isHost = false;
    private final AtomicBoolean isMyTurn = new AtomicBoolean(false);
    private GUIserver server;
    private GUIclient client;

    @Override
    public void start(Stage stage) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Local", "Local", "LAN");
        dialog.setTitle("Chess Game");
        dialog.setHeaderText("Choose Game Mode");
        dialog.setContentText("Mode:");
        String choice = dialog.showAndWait().orElse("Local");

        if (choice.equals("Local")) {
            vsComputer = askVsComputer();
            if (vsComputer) ai = new ComputerPlayer();
            isLAN = false;
            initBackend();
            buildBoardUI(stage);
        } else {
            isLAN = true;
            setupLAN(stage);
        }
    }

    private boolean askVsComputer() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Game Mode");
        a.setHeaderText("Play against computer?");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        a.getButtonTypes().setAll(yes, no);
        return a.showAndWait().orElse(no) == yes;
    }

    // ================= LAN SETUP =================
    private void setupLAN(Stage stage) {
        Label status = new Label("Host or Join");
        TextField ipField = new TextField();
        ipField.setPromptText("Host IP");
        Button hostBtn = new Button("Host (White)");
        Button joinBtn = new Button("Join (Black)");

        VBox box = new VBox(12, status, ipField, hostBtn, joinBtn);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        stage.setScene(new Scene(box, 420, 260));
        stage.setTitle("LAN Chess");
        stage.show();

        hostBtn.setOnAction(e -> startHostLAN(stage));
        joinBtn.setOnAction(e -> startClientLAN(stage, ipField.getText()));
    }

    private void startHostLAN(Stage stage) {
        isHost = true;
        isMyTurn.set(true);

        server = new GUIserver();
        server.start(5000, this::applyLANMove);

        Platform.runLater(() -> {
            initBackend();
            buildBoardUI(stage);
        });
    }

    private void startClientLAN(Stage stage, String hostIp) {
        if (hostIp.isEmpty()) return;

        isHost = false;
        isMyTurn.set(false);

        client = new GUIclient();
        client.connect(hostIp, 5000, this::applyLANMove);

        Platform.runLater(() -> {
            initBackend();
            buildBoardUI(stage);
        });
    }

    private void applyLANMove(Move m) {
        Platform.runLater(() -> {
            piece p = Board.getBoard()[m.fromRow][m.fromCol];
            if (p == null) return;
            p.move(p.rcToAlgebraic(m.toRow, m.toCol));
            refreshBoard();
            turnManager.endTurn();
            isMyTurn.set(true);
        });
    }

    private void initBackend() {
        gameBoard = new Board();
        vsComputer = isLAN ? false : vsComputer;

        whitePlayer = new Player(gamelogic.Color.WHITE, "White");
        blackPlayer = new Player(gamelogic.Color.BLACK,
                vsComputer ? "Computer" : "Black");

        turnManager = new Playerturn(gamelogic.Color.WHITE);
    }

    private void buildBoardUI(Stage stage) {
        board = new GridPane();
        board.setAlignment(Pos.CENTER);

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Rectangle sq = new Rectangle(SIZE, SIZE);
                sq.setFill((r + c) % 2 == 0 ? Color.BEIGE : Color.LIGHTBLUE);
                Text t = new Text("" + FILES.charAt(c) + (8 - r));
                StackPane cell = new StackPane(sq, t);
                int row = r, col = c;
                cell.setOnMouseClicked(e -> handleClick(row, col));
                board.add(cell, c, r);
            }
        }

        loadPieceImages();

        refreshBoard();

        stage.setScene(new Scene(board, SIZE * BOARD_SIZE + 20, SIZE * BOARD_SIZE + 20));
        stage.setTitle("Chess");
        stage.show();
    }

    private void handleClick(int row, int col) {
        gamelogic.Color turn = turnManager.getCurrentTurn();
        if (isLAN && !isMyTurn.get()) return;
        if (!isLAN && vsComputer && turn == gamelogic.Color.BLACK) return;

        piece clicked = Board.getBoard()[row][col];

        // SELECT
        if (selectedPiece == null) {
            if (clicked != null && clicked.getColor() == turn) {
                selectedPiece = clicked;
                selectedRow = row;
                selectedCol = col;
                showMoves(clicked);
            }
            return;
        }

        // MOVE
        String target = selectedPiece.rcToAlgebraic(row, col);
        boolean moved = selectedPiece.move(target);
        clearHighlights();

        if (moved) {
            refreshBoard();
            turnManager.endTurn();

            if (isLAN) {
                sendLANMove(selectedRow, selectedCol, row, col);
                isMyTurn.set(false);
            }

            if (!isLAN && vsComputer &&
                    turnManager.getCurrentTurn() == gamelogic.Color.BLACK) {
                computerMove();
            }
        }
        selectedPiece = null;
    }

    private void computerMove() {
        Move m = ai.getNextMove(gameBoard, gamelogic.Color.BLACK);
        if (m == null) return;
        piece p = Board.getBoard()[m.fromRow][m.fromCol];
        p.move(p.rcToAlgebraic(m.toRow, m.toCol));
        refreshBoard();
        turnManager.endTurn();
    }

    private void sendLANMove(int fr, int fc, int tr, int tc) {
        Move m = new Move(fr, fc, tr, tc);
        if (isHost) server.sendMove(m);
        else client.sendMove(m);
    }

    private void showMoves(piece p) {
        clearHighlights();
        for (String s : p.getPossibleMoves(Board.getBoard())) {
            int[] rc = p.algebraicToRC(s);
            StackPane cell = (StackPane) getNode(rc[1], rc[0]);
            Circle dot = new Circle(10, Color.rgb(0, 200, 0, 0.6));
            cell.getChildren().add(dot);
            moveHighlights.add(dot);
        }
    }

    private void clearHighlights() {
        for (Circle c : moveHighlights)
            ((Pane) c.getParent()).getChildren().remove(c);
        moveHighlights.clear();
    }

    private void refreshBoard() {
        board.getChildren().forEach(n -> {
            if (n instanceof StackPane sp)
                sp.getChildren().removeIf(x -> x instanceof ImageView);
        });

        piece[][] m = Board.getBoard();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (m[r][c] != null) {
                    ImageView iv = new ImageView(getImage(m[r][c]));
                    iv.setFitWidth(SIZE * 0.8);
                    iv.setFitHeight(SIZE * 0.8);
                    ((StackPane) getNode(c, r)).getChildren().add(iv);
                }
    }

    private javafx.scene.Node getNode(int c, int r) {
        for (var n : board.getChildren())
            if (GridPane.getColumnIndex(n) == c && GridPane.getRowIndex(n) == r)
                return n;
        return null;
    }

    private Image getImage(piece p) {
        return (p.getColor() == gamelogic.Color.WHITE)
                ? switch (p.getType()) {
            case PAWN -> whitePawn;
            case ROOK -> whiteRook;
            case KNIGHT -> whiteKnight;
            case BISHOP -> whiteBishop;
            case QUEEN -> whiteQueen;
            case KING -> whiteKing;
        }
                : switch (p.getType()) {
            case PAWN -> blackPawn;
            case ROOK -> blackRook;
            case KNIGHT -> blackKnight;
            case BISHOP -> blackBishop;
            case QUEEN -> blackQueen;
            case KING -> blackKing;
        };
    }

    private void loadPieceImages() {
        whitePawn   = loadPieceImages("/Pawn.jpeg");
        whiteRook   = loadPieceImages("/white rook.jpeg");
        whiteKnight = loadPieceImages("/white horse.jpeg");
        whiteBishop = loadPieceImages("/white bishop.jpeg");
        whiteQueen  = loadPieceImages("/white queen.jpeg");
        whiteKing   = loadPieceImages("/white king.jpeg");

        blackPawn   = loadPieceImages("/black pawn.jpeg");
        blackRook   = loadPieceImages("/black rook.jpeg");
        blackKnight = loadPieceImages("/black horse.jpeg");
        blackBishop = loadPieceImages("/black bishop.jpeg");
        blackQueen  = loadPieceImages("/black queen.jpeg");
        blackKing   = loadPieceImages("/black king.jpeg");
    }

    private Image loadPieceImages(String classpathPath) {
        try {
            var in = getClass().getResourceAsStream(classpathPath);
            return in != null ? new Image(in) : null;
        } catch (Exception e) {
            return null;
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}