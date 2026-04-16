package Frontend;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

public class Loginpage extends Application {

    // File where users will be stored
    private static final Path USERS_FILE = Paths.get("users.txt");

    private static final String BACKGROUND_IMAGE_PATH = Loginpage.class.getResource("/Dash.png").toExternalForm();

    private TextField usernameField;
    private TextField emailField;
    private TextField ratingField;

    @Override
    public void start(Stage stage) {
        ensureUsersFileExists();

        // ---------- UI ----------
        Label heading = new Label("Chess Game Login");
        heading.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 2);");

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        emailField = new TextField();
        emailField.setPromptText("Email");

        ratingField = new TextField();
        ratingField.setPromptText("Rating (integer)");

        // Style text fields
        String textFieldStyle = """
                -fx-background-color: rgba(255, 255, 255, 0.9);
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-padding: 8;
                -fx-font-size: 14px;
                """;

        usernameField.setStyle(textFieldStyle);
        emailField.setStyle(textFieldStyle);
        ratingField.setStyle(textFieldStyle);

        // Labels
        Label uLbl = new Label("Username");
        Label eLbl = new Label("Email");
        Label rLbl = new Label("Rating");

        String labelStyle = "-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;";
        uLbl.setStyle(labelStyle);
        eLbl.setStyle(labelStyle);
        rLbl.setStyle(labelStyle);

        // Buttons
        Button loginBtn = new Button("Login");
        Button registerBtn = new Button("Register");

        loginBtn.setPrefWidth(140);
        registerBtn.setPrefWidth(140);

        loginBtn.setStyle(btnStyle("#1B5E20"));      // dark green
        registerBtn.setStyle(btnStyle("#2E7D32"));   // green

        // Hover effects
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(btnStyle("#145214")));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(btnStyle("#1B5E20")));
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(btnStyle("#256628")));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(btnStyle("#2E7D32")));

        loginBtn.setOnAction(e -> handleLogin(stage));
        registerBtn.setOnAction(e -> handleRegister());

        HBox btnRow = new HBox(12, loginBtn, registerBtn);
        btnRow.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        form.add(uLbl, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(eLbl, 0, 1);
        form.add(emailField, 1, 1);
        form.add(rLbl, 0, 2);
        form.add(ratingField, 1, 2);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPrefWidth(90);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c0, c1);

        VBox card = new VBox(20, heading, form, btnRow);
        card.setPadding(new Insets(30));
        card.setMaxWidth(450);
        card.setAlignment(Pos.CENTER);


        StackPane root = new StackPane(card);
        root.setPadding(new Insets(40));

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
            root.setBackground(new Background(background));
        } catch (Exception e) {
            // Fallback to gradient if image not found
            System.err.println("Background image not found: " + e.getMessage());
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
        }

        Scene scene = new Scene(root, 700, 500);
        stage.setTitle("Chess Game - Login / Register");
        stage.setScene(scene);
        stage.show();
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String ratingText = ratingField.getText().trim();

        if (username.isEmpty() || email.isEmpty() || ratingText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Data", "Please fill all fields.");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingText);
        } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Invalid Rating", "Rating must be an integer.");
            return;
        }

        // If username already exists -> don't register again
        if (findUserByUsername(username).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Already Registered", "This username already exists.");
            return;
        }

        // Save as: username|email|rating
        String line = username + "|" + email + "|" + rating;

        try (BufferedWriter bw = Files.newBufferedWriter(USERS_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(line);
            bw.newLine();
            showAlert(Alert.AlertType.INFORMATION, "Registered", "User saved successfully!");
            clearFields();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not write to users.txt\n" + e.getMessage());
        }
    }

    private void handleLogin(Stage stage) {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Username", "Enter username to login.");
            return;
        }

        Optional<UserRecord> user = findUserByUsername(username);

        if (user.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Not Registered", "User is not registered.");
            return;
        }

        // SUCCESS -> open chessboard
        openChessboard(stage, user.get());
    }

    private void openChessboard(Stage stage, UserRecord user) {
        // Open Dashboard instead of direct chessboard
        DashBoardView dashboard = new DashBoardView(user.username(), user.rating(), stage);

        Scene dashboardScene = new Scene(dashboard, 900, 700);
        stage.setTitle("Chess Dashboard - " + user.username());
        stage.setScene(dashboardScene);
        stage.show();
    }

    private Optional<UserRecord> findUserByUsername(String username) {
        try (BufferedReader br = Files.newBufferedReader(USERS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // username|email|rating
                String[] parts = line.split("\\|");
                if (parts.length != 3) continue;

                String u = parts[0].trim();
                String e = parts[1].trim();
                int r;
                try { r = Integer.parseInt(parts[2].trim()); }
                catch (NumberFormatException ex) { continue; }

                if (u.equalsIgnoreCase(username)) {
                    return Optional.of(new UserRecord(u, e, r));
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not read users.txt\n" + e.getMessage());
        }
        return Optional.empty();
    }

    /** Update the stored rating for a given username in users.txt. */
    public static void updateRating(String username, int newRating) {
        if (username == null || username.isBlank()) return;
        try {
            Path file = USERS_FILE;
            if (Files.notExists(file)) return;
            List<String> lines = Files.readAllLines(file);
            List<String> updated = new java.util.ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length == 3 && parts[0].trim().equalsIgnoreCase(username)) {
                    updated.add(parts[0].trim() + "|" + parts[1].trim() + "|" + newRating);
                } else {
                    updated.add(line);
                }
            }
            Files.write(file, updated);
        } catch (IOException ignored) {}
    }

    private void ensureUsersFileExists() {
        try {
            if (Files.notExists(USERS_FILE)) {
                Path parent = USERS_FILE.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.createFile(USERS_FILE);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "File Error", "Could not create users.txt\n" + e.getMessage());
        }
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        ratingField.clear();
    }

    private String btnStyle(String bg) {
        return """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-background-radius: 10;
                -fx-padding: 12 20 12 20;
                -fx-font-size: 14px;
                -fx-cursor: hand;
                """.formatted(bg);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // simple record to store loaded data
    private record UserRecord(String username, String email, int rating) {}

    public static void main(String[] args) {
        launch(args);
    }
}