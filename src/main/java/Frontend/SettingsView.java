package Frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SettingsView extends BorderPane {

    // Path to your background image
    private static final String BACKGROUND_IMAGE_PATH = "file:src/main/resources/Dash.png";

    private String username;
    private int rating;
    private Stage stage;

    // Settings controls
    private Slider soundVolumeSlider;
    private Slider musicVolumeSlider;
    private CheckBox enableSoundCheckBox;
    private CheckBox enableMusicCheckBox;
    private ComboBox<String> themeComboBox;
    private ComboBox<String> difficultyComboBox;
    private CheckBox showHintsCheckBox;
    private CheckBox highlightMovesCheckBox;

    public SettingsView(String username, int rating, Stage stage) {
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
            System.err.println("Background image not found: " + e.getMessage());
            this.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f2027, #203a43, #2c5364);");
        }

        // Top section - Header
        VBox topSection = createTopSection();
        this.setTop(topSection);

        // Center section - Settings
        VBox centerSection = createCenterSection();
        this.setCenter(centerSection);

        // Bottom section - Action buttons
        HBox bottomSection = createBottomSection();
        this.setBottom(bottomSection);
    }

    private VBox createTopSection() {
        Label titleLabel = new Label("⚙ Settings");
        titleLabel.setStyle("""
                -fx-font-size: 36px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 3);
                """);

        VBox topBox = new VBox(titleLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(30, 20, 10, 20));

        return topBox;
    }

    private VBox createCenterSection() {
        // Audio Settings Section
        VBox audioSection = createAudioSettings();

        // Game Settings Section
        VBox gameSection = createGameSettings();

        // Visual Settings Section
        VBox visualSection = createVisualSettings();

        // Main container
        VBox settingsContainer = new VBox(20, audioSection, gameSection, visualSection);
        settingsContainer.setAlignment(Pos.TOP_CENTER);
        settingsContainer.setPadding(new Insets(30));
        settingsContainer.setMaxWidth(700);
        settingsContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.75);
                -fx-background-radius: 20;
                -fx-border-radius: 20;
                -fx-border-color: rgba(255, 255, 255, 0.3);
                -fx-border-width: 2;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 5);
                """);

        ScrollPane scrollPane = new ScrollPane(settingsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(20));

        VBox centerBox = new VBox(scrollPane);
        centerBox.setAlignment(Pos.CENTER);

        return centerBox;
    }

    private VBox createAudioSettings() {
        Label sectionTitle = new Label("🔊 Audio Settings");
        sectionTitle.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFD700;
                """);

        // Enable Sound
        enableSoundCheckBox = new CheckBox("Enable Sound Effects");
        enableSoundCheckBox.setSelected(true);
        enableSoundCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Sound Volume
        Label soundLabel = new Label("Sound Volume");
        soundLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        soundVolumeSlider = new Slider(0, 100, 70);
        soundVolumeSlider.setShowTickMarks(true);
        soundVolumeSlider.setShowTickLabels(true);
        soundVolumeSlider.setMajorTickUnit(25);

        // Enable Music
        enableMusicCheckBox = new CheckBox("Enable Background Music");
        enableMusicCheckBox.setSelected(true);
        enableMusicCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Music Volume
        Label musicLabel = new Label("Music Volume");
        musicLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        musicVolumeSlider = new Slider(0, 100, 50);
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setMajorTickUnit(25);

        VBox audioBox = new VBox(15,
                sectionTitle,
                enableSoundCheckBox,
                soundLabel,
                soundVolumeSlider,
                enableMusicCheckBox,
                musicLabel,
                musicVolumeSlider
        );
        audioBox.setPadding(new Insets(15));
        audioBox.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.1);
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                """);

        return audioBox;
    }

    private VBox createGameSettings() {
        Label sectionTitle = new Label("🎮 Game Settings");
        sectionTitle.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFD700;
                """);

        // Difficulty
        Label difficultyLabel = new Label("Bot Difficulty");
        difficultyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        difficultyComboBox = new ComboBox<>();
        difficultyComboBox.getItems().addAll("Easy", "Medium", "Hard", "Expert");
        difficultyComboBox.setValue("Medium");
        difficultyComboBox.setPrefWidth(200);

        // Show Hints
        showHintsCheckBox = new CheckBox("Show Move Hints");
        showHintsCheckBox.setSelected(true);
        showHintsCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Highlight Moves
        highlightMovesCheckBox = new CheckBox("Highlight Legal Moves");
        highlightMovesCheckBox.setSelected(true);
        highlightMovesCheckBox.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        VBox gameBox = new VBox(15,
                sectionTitle,
                difficultyLabel,
                difficultyComboBox,
                showHintsCheckBox,
                highlightMovesCheckBox
        );
        gameBox.setPadding(new Insets(15));
        gameBox.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.1);
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                """);

        return gameBox;
    }

    private VBox createVisualSettings() {
        Label sectionTitle = new Label(" Visual Settings");
        sectionTitle.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-text-fill: #FFD700;
                """);

        // Theme
        Label themeLabel = new Label("Board Theme");
        themeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll("Classic", "Wood", "Marble", "Modern", "Dark");
        themeComboBox.setValue("Classic");
        themeComboBox.setPrefWidth(200);

        VBox visualBox = new VBox(15,
                sectionTitle,
                themeLabel,
                themeComboBox
        );
        visualBox.setPadding(new Insets(15));
        visualBox.setStyle("""
                -fx-background-color: rgba(255, 255, 255, 0.1);
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                """);

        return visualBox;
    }

    private HBox createBottomSection() {
        Button saveButton = new Button("💾 Save Settings");
        saveButton.setPrefWidth(180);
        saveButton.setStyle("""
                -fx-background-color: #2E7D32;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 12 20;
                -fx-cursor: hand;
                """);
        saveButton.setOnAction(e -> handleSaveSettings());

        Button resetButton = new Button(" Reset Defaults");
        resetButton.setPrefWidth(180);
        resetButton.setStyle("""
                -fx-background-color: #FF9800;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 12 20;
                -fx-cursor: hand;
                """);
        resetButton.setOnAction(e -> handleResetSettings());

        Button backButton = new Button("⬅ Back to Dashboard");
        backButton.setPrefWidth(180);
        backButton.setStyle("""
                -fx-background-color: #2196F3;
                -fx-text-fill: white;
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 12 20;
                -fx-cursor: hand;
                """);
        backButton.setOnAction(e -> handleBackToDashboard());

        HBox buttonBox = new HBox(20, backButton, resetButton, saveButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 40, 30, 40));
        buttonBox.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.5);
                -fx-background-radius: 15 15 0 0;
                """);

        return buttonBox;
    }

    private void handleSaveSettings() {
        System.out.println("Settings saved:");
        System.out.println("Sound: " + enableSoundCheckBox.isSelected());
        System.out.println("Sound Volume: " + soundVolumeSlider.getValue());
        System.out.println("Music: " + enableMusicCheckBox.isSelected());
        System.out.println("Music Volume: " + musicVolumeSlider.getValue());
        System.out.println("Theme: " + themeComboBox.getValue());
        System.out.println("Difficulty: " + difficultyComboBox.getValue());
        System.out.println("Show Hints: " + showHintsCheckBox.isSelected());
        System.out.println("Highlight Moves: " + highlightMovesCheckBox.isSelected());

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your settings have been saved successfully!");
        alert.showAndWait();
    }

    private void handleResetSettings() {
        soundVolumeSlider.setValue(70);
        musicVolumeSlider.setValue(50);
        enableSoundCheckBox.setSelected(true);
        enableMusicCheckBox.setSelected(true);
        themeComboBox.setValue("Classic");
        difficultyComboBox.setValue("Medium");
        showHintsCheckBox.setSelected(true);
        highlightMovesCheckBox.setSelected(true);

        System.out.println("Settings reset to defaults");
    }

    private void handleBackToDashboard() {
        DashBoardView dashboard = new DashBoardView(username, rating, stage);
        Scene dashboardScene = new Scene(dashboard, 900, 700);
        stage.setTitle("Chess Dashboard - " + username);
        stage.setScene(dashboardScene);
    }
}