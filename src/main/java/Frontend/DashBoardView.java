package Frontend;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.application.Platform;


public class DashBoardView extends BorderPane {

    private static final String BACKGROUND_IMAGE_PATH = DashBoardView.class.getResource("/Dash.png").toExternalForm();

    private String username;
    private int rating;
    private Stage stage;
    private boolean isCollapsed = false;

    public DashBoardView(String username, int rating, Stage stage) {
        this.username = username;
        this.rating = rating;
        this.stage = stage;

        setupUI();
    }

    private void setupUI() {
        // Set background image
        try {
            Image bgImage = new Image(BACKGROUND_IMAGE_PATH);
            BackgroundImage background = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
            );
            this.setBackground(new Background(background));
        } catch (Exception e) {
            // Fallback to gradient if image not found
            System.err.println("Background image not found: " + e.getMessage());
            this.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f2027, #203a43, #2c5364);");
        }

        // Top section - User info
        VBox topSection = createTopSection();
        this.setTop(topSection);

        // Center section - Game mode buttons
        VBox centerSection = createCenterSection();
        this.setCenter(centerSection);

        // Bottom section - Utility buttons
        HBox bottomSection = createBottomSection();
        this.setBottom(bottomSection);
    }

    private VBox createTopSection() {
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.setStyle("""
                -fx-font-size: 32px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        Label ratingLabel = new Label("Rating: " + rating);
        ratingLabel.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFD700;
                """);

        VBox topBox = new VBox(10, welcomeLabel, ratingLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(40, 20, 20, 20));

        return topBox;
    }

    private VBox createCenterSection() {
        // Title
        Label titleLabel = new Label("Select Game Mode");
        titleLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 2);
                """);

        // Create buttons
        Button pvpButton = createGameModeButton("Player vs Player", "👥");
        Button pvbButton = createGameModeButton("Player vs Bot", "🤖");
        Button lanButton = createGameModeButton("LAN Player", "🌐");

        // Button actions
        pvpButton.setOnAction(e -> handlePlayerVsPlayer());
        pvbButton.setOnAction(e -> handlePlayerVsBot());
        lanButton.setOnAction(e -> handleLANPlayer());

        // GridPane for buttons
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(20);
        buttonGrid.setVgap(20);
        buttonGrid.setAlignment(Pos.CENTER);

        // Add buttons to grid
        buttonGrid.add(pvpButton, 0, 0);
        buttonGrid.add(pvbButton, 1, 0);
        buttonGrid.add(lanButton, 0, 1);
        GridPane.setColumnSpan(lanButton, 2);
        GridPane.setHalignment(lanButton, javafx.geometry.HPos.CENTER);

        // Make buttons same width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        buttonGrid.getColumnConstraints().addAll(col1, col2);

        // Container with semi-transparent background
        VBox container = new VBox(30, titleLabel, buttonGrid);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));
        container.setMaxWidth(700);
        container.setMaxHeight(500);


        VBox centerBox = new VBox(container);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        return centerBox;
    }

    private Button createGameModeButton(String text, String emoji) {
        Button button = new Button(emoji + "  " + text);
        button.setPrefWidth(280);
        button.setPrefHeight(100);
        button.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #2E7D32, #1B5E20);
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-border-color: rgba(255, 255, 255, 0.4);
                -fx-border-width: 2;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);
                """);

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #388E3C, #2E7D32);
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-border-color: rgba(255, 255, 255, 0.6);
                -fx-border-width: 2;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 6);
                -fx-scale-x: 1.05;
                -fx-scale-y: 1.05;
                """));

        button.setOnMouseExited(e -> button.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #2E7D32, #1B5E20);
                -fx-text-fill: white;
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-border-color: rgba(255, 255, 255, 0.4);
                -fx-border-width: 2;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);
                """));

        return button;
    }

    private HBox createBottomSection() {
        // Create utility button containers with labels
        VBox collapseBox = createUtilityButtonWithLabel("⬇", "Collapse", "#FF9800");
        VBox profileBox = createUtilityButtonWithLabel("👤", "Profile", "#2196F3");
        VBox settingsBox = createUtilityButtonWithLabel("⚙", "Settings", "#9C27B0");
        VBox closeBox = createUtilityButtonWithLabel("✖", "Close", "#D32F2F");

        // Get buttons from VBox containers
        Button collapseButton = (Button) collapseBox.getChildren().get(0);
        Button profileButton = (Button) profileBox.getChildren().get(0);
        Button settingsButton = (Button) settingsBox.getChildren().get(0);
        Button closeButton = (Button) closeBox.getChildren().get(0);

        // Button actions
        collapseButton.setOnAction(e -> handleCollapse(collapseButton, (Label) collapseBox.getChildren().get(1)));
        profileButton.setOnAction(e -> handleProfile());
        settingsButton.setOnAction(e -> handleSettings());
        closeButton.setOnAction(e -> handleClose());

        // Container for button boxes
        HBox buttonContainer = new HBox(25, collapseBox, profileBox, settingsBox, closeBox);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 40, 30, 40));


        return buttonContainer;
    }

    private VBox createUtilityButtonWithLabel(String icon, String labelText, String color) {
        // Create button with icon
        Button button = new Button(icon);
        button.setPrefWidth(80);
        button.setPrefHeight(80);
        button.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-size: 32px;
                -fx-font-weight: bold;
                -fx-background-radius: 15;
                -fx-border-radius: 15;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);
                """, color));

        // Create label
        Label label = new Label(labelText);
        label.setStyle("""
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);
                """);

        // Hover effect
        button.setOnMouseEntered(e -> {
            String darkerColor = getDarkerColor(color);
            button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: white;
                    -fx-font-size: 32px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 15;
                    -fx-border-radius: 15;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0, 0, 4);
                    -fx-scale-x: 1.08;
                    -fx-scale-y: 1.08;
                    """, darkerColor));
            label.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-size: 15px;
                    -fx-font-weight: bold;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);
                    """);
        });

        button.setOnMouseExited(e -> {
            button.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: white;
                    -fx-font-size: 32px;
                    -fx-font-weight: bold;
                    -fx-background-radius: 15;
                    -fx-border-radius: 15;
                    -fx-cursor: hand;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);
                    """, color));
            label.setStyle("""
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 5, 0, 0, 1);
                    """);
        });

        // VBox to hold button and label
        VBox container = new VBox(8, button, label);
        container.setAlignment(Pos.CENTER);

        return container;
    }



    private String getDarkerColor(String color) {
        return switch (color) {
            case "#FF9800" -> "#F57C00";
            case "#2196F3" -> "#1976D2";
            case "#9C27B0" -> "#7B1FA2";
            case "#D32F2F" -> "#C62828";
            default -> color;
        };
    }

    private void handlePlayerVsPlayer() {
        System.out.println("Player vs Player mode selected");

        try {
            chessboard chessGame = new chessboard();// ✅ Application class

            chessGame.start(stage);                   // ✅ runs chessboard
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private void handlePlayerVsBot() {
        System.out.println("Player vs Bot mode selected");
        chessboardAI chessGame = new chessboardAI();
        chessGame.start(stage);
    }

    private void handleLANPlayer() {
        System.out.println("LAN Player mode selected");
        // Open LAN connection dialog or game
        chesslan ch = new chesslan();
           ch.start(stage);
//        Scene chessScene = new Scene(chessboard, 900, 700);
//        stage.setTitle("Chess - LAN Game");
//        stage.setScene(chessScene);
    }

    // Handler methods for utility buttons
    private void handleCollapse(Button collapseButton, Label collapseLabel) {
        isCollapsed = !isCollapsed;

        if (isCollapsed) {
            // Minimize window
            stage.setIconified(true);
            collapseButton.setText("⬆");
            collapseLabel.setText("Expand");
            System.out.println("Window collapsed");
        } else {
            // Restore window
            stage.setIconified(false);
            collapseButton.setText("⬇");
            collapseLabel.setText("Collapse");
            System.out.println("Window expanded");
        }
    }

    private void handleProfile() {
        System.out.println("Profile button clicked");
        // Open profile view
        ProfileView profileView = new ProfileView(username, rating, stage);
        Scene profileScene = new Scene(profileView, 900, 700);
        stage.setTitle("Chess - Profile");
        stage.setScene(profileScene);
    }

    private void handleSettings() {
        System.out.println("Settings button clicked");
        // Open settings view
        SettingsView settingsView = new SettingsView(username, rating, stage);
        Scene settingsScene = new Scene(settingsView, 900, 700);
        stage.setTitle("Chess - Settings");
        stage.setScene(settingsScene);
    }

    private void handleClose() {
        System.out.println("Closing application...");
        // Close the application
        Platform.exit();
        System.exit(0);
    }
}
