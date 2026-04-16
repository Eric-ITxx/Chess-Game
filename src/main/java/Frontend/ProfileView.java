package Frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import makingstats.LeaderBoard;
import makingstats.Players;

public class ProfileView extends StackPane {


    private static final String BACKGROUND_IMAGE_PATH = ProfileView.class.getResource("/Dash.png").toExternalForm();

    private String username;
    private int rating;
    private Stage stage;

    // Stats (for overview)
    private int totalGames = 0;
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    // Header controls
    private Label nameLabel;
    private TextField nameField;
    private Button editBtn;
    private boolean editing = false;

    public ProfileView(String username, int rating, Stage stage) {
        this.username = username;
        this.rating = rating;
        this.stage = stage;

        buildUI();
    }

    private void buildUI() {

        // ---------- Background ----------
        ImageView bg = new ImageView();
        try {
            Image bgImg = new Image(BACKGROUND_IMAGE_PATH);
            bg.setImage(bgImg);
        } catch (Exception e) {
            // fallback if image not found
        }
        bg.setPreserveRatio(false);
        bg.fitWidthProperty().bind(widthProperty());
        bg.fitHeightProperty().bind(heightProperty());

        // ---------- Main centered container (auto-resize) ----------
        BorderPane main = new BorderPane();
        main.setMaxWidth(1050);
        main.setPadding(new Insets(18));
        BorderPane.setMargin(main, new Insets(18));

        // Center main inside screen
        StackPane centerWrap = new StackPane(main);
        centerWrap.setAlignment(Pos.TOP_CENTER);

        // transparent overall
        main.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        // ---------- Top header like screenshot ----------
        VBox topHeader = buildHeader();
        main.setTop(topHeader);

        // ---------- Tabs ----------
        TabPane tabs = buildTabs();
        main.setCenter(tabs);

        // Add to root
        getChildren().addAll(bg, centerWrap);
    }

    private VBox buildHeader() {

        // Row 1 (Back + Profile header area)
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("⬅ Back");
        backBtn.setStyle(buttonStyle("#2D7EF7"));
        backBtn.setOnAction(e -> goToDashboard());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        editBtn = new Button("Edit Profile");
        editBtn.setStyle(buttonStyle("#E5E7EB"));
        editBtn.setTextFill(Color.BLACK);
        editBtn.setOnAction(e -> toggleEditName());

        topRow.getChildren().addAll(backBtn, spacer, editBtn);

        // Row 2 (Avatar + Name + flags + subtitle)
        HBox profileRow = new HBox(16);
        profileRow.setAlignment(Pos.CENTER_LEFT);
        profileRow.setPadding(new Insets(14, 18, 14, 18));
        profileRow.setMaxWidth(Double.MAX_VALUE);
//        profileRow.setStyle(cardStyle());

        // Avatar (left)
        StackPane avatar = new StackPane();
        avatar.setMinSize(84, 84);
        avatar.setMaxSize(84, 84);
        avatar.setStyle("""
                -fx-background-color: rgba(0,0,0,0.55);
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(255,255,255,0.18);
                -fx-border-width: 1;
                """);
        Label horse = new Label("♞");
        horse.setTextFill(Color.WHITE);
        horse.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 40));
        avatar.getChildren().add(horse);

        // Name line (editable)
        VBox nameBox = new VBox(6);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        HBox nameLine = new HBox(10);
        nameLine.setAlignment(Pos.CENTER_LEFT);

        nameLabel = new Label(username);
        nameLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        nameLabel.setTextFill(Color.WHITE);


        Label pkFlag = new Label("🇵🇰");
        pkFlag.setFont(Font.font(18));

        nameField = new TextField(username);
        nameField.setVisible(false);
        nameField.setManaged(false);
        nameField.setStyle("""
                -fx-background-color: rgba(255,255,255,0.12);
                -fx-text-fill: white;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-color: rgba(255,255,255,0.25);
                -fx-padding: 8 10;
                """);

        nameLine.getChildren().addAll(nameLabel, pkFlag, nameField);


        Label subLine = new Label("Some Guy    Behind a laptop");
        subLine.setTextFill(Color.rgb(220, 220, 220));
        subLine.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        nameBox.getChildren().addAll(nameLine, subLine);

        profileRow.getChildren().addAll(avatar, nameBox);

        VBox header = new VBox(12, topRow, profileRow);
        header.setPadding(new Insets(10, 6, 10, 6));
        return header;
    }

    private TabPane buildTabs() {

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("""
                -fx-background-color: transparent;
                -fx-tab-min-width: 140;
                """);

        Tab overview = new Tab("Overview", buildOverviewPane());
        Tab games = new Tab("Games", buildGamesPane());
        Tab stats = new Tab("Stats", buildStatsPane());
        Tab clubs = new Tab("Clubs", buildClubsPane());

        tabPane.getTabs().addAll(overview, games, stats, clubs);
        return tabPane;
    }

    // -------------------- OVERVIEW --------------------
    private Pane buildOverviewPane() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.TOP_CENTER);

        // Stats cards row like screenshot positioning
        HBox cards = new HBox(14);
        cards.setAlignment(Pos.CENTER);
        cards.setFillHeight(true);

        cards.getChildren().addAll(
                statCard("Total Games", String.valueOf(totalGames)),
                statCard("Wins", String.valueOf(wins)),
                statCard("Losses", String.valueOf(losses)),
                statCard("Draws", String.valueOf(draws))
        );

        VBox info = new VBox(10);
        info.setPadding(new Insets(16));
        info.setMaxWidth(980);
//        info.setStyle(cardStyle());

        Label about = new Label("About");
        about.setTextFill(Color.WHITE);
        about.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label aboutText = new Label("i like chess");
        aboutText.setWrapText(true);
        aboutText.setTextFill(Color.rgb(230, 230, 230));

        info.getChildren().addAll(about, aboutText);

        root.getChildren().addAll(cards, info);
        return root;
    }

    private VBox statCard(String title, String value) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(14));
        box.setPrefWidth(220);
        box.setMinHeight(90);
        box.setStyle("""
                -fx-background-color: rgba(0,0,0,0.55);
                -fx-background-radius: 16;
                -fx-border-radius: 16;
                -fx-border-color: rgba(255,255,255,0.15);
                -fx-border-width: 1;
                """);

        Label t = new Label(title);
        t.setTextFill(Color.rgb(200, 200, 200));
        t.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 13));

        Label v = new Label(value);
        v.setTextFill(Color.WHITE);
        v.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));

        box.getChildren().addAll(t, v);
        return box;
    }

    // -------------------- GAMES --------------------
    private Pane buildGamesPane() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setMaxWidth(980);


        Label title = new Label("Game Modes");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));

        Button pvp = new Button("PvsP");
        pvp.setStyle(buttonStyle("#22C55E"));

        Button bot = new Button("PvsBot");
        bot.setStyle(buttonStyle("#F59E0B"));

        Button lan = new Button("PvsLAN");
        lan.setStyle(buttonStyle("#A855F7"));

        HBox row = new HBox(14, pvp, bot, lan);
        row.setAlignment(Pos.CENTER_LEFT);

        Label note = new Label("Only these three game types are shown (as you requested).");
        note.setTextFill(Color.rgb(210, 210, 210));

        card.getChildren().addAll(title, row, note);
        root.getChildren().add(card);

        return root;
    }

    // -------------------- STATS / LEADERBOARD --------------------
    private Pane buildStatsPane() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setMaxWidth(980);


        Label title = new Label("Leaderboard");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));

        // Update and read leaderboard list
        try {
            LeaderBoard.updateBoard();
        } catch (Exception ignored) {}

        TableView<Players.PlayerData> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: rgba(255,255,255,0.18);
                -fx-border-width: 1;
                """);

        TableColumn<Players.PlayerData, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Players.PlayerData, String> ratingCol = new TableColumn<>("Rating");
        ratingCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getRating())));

        TableColumn<Players.PlayerData, String> winsCol = new TableColumn<>("Wins");
        winsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getWins())));

        table.getColumns().addAll(nameCol, ratingCol, winsCol);

        // fill items
        table.getItems().setAll(Players.getPlayers());


        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Players.PlayerData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.getName() != null && item.getName().equalsIgnoreCase(username)) {
                    setStyle("""
                            -fx-background-color: rgba(34,197,94,0.22);
                            -fx-font-weight: bold;
                            """);
                } else {
                    setStyle("");
                }
            }
        });

        if (table.getItems().isEmpty()) {
            Label empty = new Label("No leaderboard data yet. Add players to Players list during gameplay.");
            empty.setTextFill(Color.rgb(210, 210, 210));
            card.getChildren().addAll(title, empty);
        } else {
            card.getChildren().addAll(title, table);
        }

        root.getChildren().add(card);
        return root;
    }

    // -------------------- CLUBS --------------------
    private Pane buildClubsPane() {

        VBox root = new VBox(16);
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.TOP_CENTER);

        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setMaxWidth(980);
//        card.setStyle(cardStyle());

        Label title = new Label("Clubs");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));

        ListView<String> clubs = new ListView<>();
        clubs.getItems().addAll(
                "Karachi Chess Club",
                "Lahore Rapid Kings",
                "Islamabad Blitz Arena",
                "Sindh Strategists",
                "Punjab Checkmates"
        );
        clubs.setStyle("""
                -fx-background-color: rgba(255,255,255,0.06);
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-color: rgba(255,255,255,0.18);
                -fx-border-width: 1;
                """);

        card.getChildren().addAll(title, clubs);
        root.getChildren().add(card);

        return root;
    }

    // -------------------- Edit Name --------------------
    private void toggleEditName() {
        editing = !editing;

        if (editing) {
            editBtn.setText("Save");
            nameField.setText(username);

            nameLabel.setVisible(false);
            nameLabel.setManaged(false);

            nameField.setVisible(true);
            nameField.setManaged(true);
            nameField.requestFocus();

        } else {
            editBtn.setText("Edit Profile");
            String newName = nameField.getText().trim();
            if (!newName.isBlank()) {
                username = newName;
                nameLabel.setText(username);
            }

            nameField.setVisible(false);
            nameField.setManaged(false);

            nameLabel.setVisible(true);
            nameLabel.setManaged(true);
        }
    }

    // -------------------- Navigation --------------------
    private void goToDashboard() {
        DashBoardView dashboard = new DashBoardView(username, rating, stage);
        Scene dashboardScene = new Scene(dashboard, 900, 700);
        stage.setTitle("Chess Dashboard - " + username);
        stage.setScene(dashboardScene);
    }



    private String buttonStyle(String colorHex) {
        return """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-background-radius: 12;
                -fx-padding: 10 16;
                -fx-cursor: hand;
                """.formatted(colorHex);
    }
}
