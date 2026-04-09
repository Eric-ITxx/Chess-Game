package Driver;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Theoryassignment3 extends Application {

    @Override
    public void start(Stage stage) {

        // ===== OUTER BORDERPANE (RED) =====
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(1100, 620);
        borderPane.setStyle("-fx-background-color: white;");
        borderPane.setBorder(new Border(new BorderStroke(
                Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(4))));


        AnchorPane anchorPane = new AnchorPane();
        borderPane.setCenter(anchorPane);

        // ===== TOP HBOX HEADER (YELLOW) =====
        HBox header = new HBox(5);
        header.setPadding(new Insets(14, 8, 8, 4));
        header.setAlignment(Pos.TOP_LEFT);
        header.setStyle("-fx-background-color: #283546;");   // dark bar
        header.setBorder(new Border(new BorderStroke(
                Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(4))));

        Label title = new Label("JavaFX Layout Practice");
        title.setFont(Font.font("Arial", 16));
        title.setTextFill(Color.WHITE);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(260);


        header.getChildren().addAll(title, searchField);

        AnchorPane.setTopAnchor(header, 10.0);
        AnchorPane.setLeftAnchor(header, 2.0);
        AnchorPane.setRightAnchor(header, 2.0);
        anchorPane.getChildren().add(header);

        // ===== MAIN CONTENT HBOX (orange outline = VBOX used inside) =====
        HBox mainRow = new HBox(10);
        mainRow.setPadding(new Insets(4));
        mainRow.setAlignment(Pos.CENTER);

        AnchorPane.setTopAnchor(mainRow, 60.0);
        AnchorPane.setLeftAnchor(mainRow, 0.0);
        AnchorPane.setRightAnchor(mainRow, 10.0);
        AnchorPane.setBottomAnchor(mainRow, 60.0);
        anchorPane.getChildren().add(mainRow);

        // ===== LEFT USER INFO VBOX (ORANGE) =====
        VBox userBox = new VBox(4);
        userBox.setPadding(new Insets(15));
        userBox.setPrefWidth(290);
        userBox.setStyle("-fx-background-color: #eceff1;");
        userBox.setBorder(new Border(new BorderStroke(
                Color.ORANGE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));

        Label userInfoLbl = new Label("User Information");
        userInfoLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        HBox userInfoBox = new HBox(2);
        userInfoBox.setSpacing(5);
        Label nameLbl = new Label("Name:");
        nameLbl.setFont(Font.font("Arial", 12));
        TextField nameField = new TextField();
        nameField.setPromptText("Enter name");
         userInfoBox.getChildren().addAll(nameLbl, nameField);
         userInfoBox.setBorder(new Border(new BorderStroke(
                 Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3.5))));
        Label emailLbl = new Label("Email:");
        emailLbl.setFont(Font.font("Arial", 12));
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        HBox user = new HBox(2);
        user.getChildren().addAll(emailLbl, emailField);
        user.setBorder(new Border(new BorderStroke(
                Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3.5))));
        user.setSpacing(7);
        Button updateBtn = new Button("Update Profile");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setStyle("-fx-background-color: #1b90e0; -fx-text-fill: white;");

        userBox.getChildren().addAll(
                userInfoLbl,
                userInfoBox,user,
                updateBtn
        );

        // ===== CENTER STACKPANE (LIGHT GREEN OUTLINE) =====
        StackPane centerStack = new StackPane();
        centerStack.setPadding(new Insets(2, 2, 2, 2));
        centerStack.setBorder(new Border(new BorderStroke(
                Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        centerStack.setStyle("-fx-background-color: #e4f7f1;");
        HBox.setHgrow(centerStack, Priority.ALWAYS);

        GridPane grid = new GridPane();
        grid.setBorder(new Border(new BorderStroke(
                Color.DARKGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        grid.setPrefSize(600, 380);
        grid.setPadding(new Insets(3, 3, 10, 2));
        grid.setStyle("-fx-background-color: transparent;");

        StackPane pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.setBorder(new Border(new BorderStroke(
                        Color.LIMEGREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        pane.setPrefSize(500,360);
        grid.getChildren().addAll(pane);


        // --- VBox inside GridPane for dashboard content (no extra border) ---
        VBox dashBox = new VBox(4);
        dashBox.setPadding(new Insets(2, 25, 25, 2));
        dashBox.setAlignment(Pos.CENTER);
        dashBox.setBorder(new Border(new BorderStroke(
                Color.ORANGE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
         pane.getChildren().addAll(dashBox);

        Label dashTitle = new Label("Dashboard Center Area");
        dashTitle.setFont(Font.font("Arial", FontWeight.BOLD,20));
        dashTitle.setTextFill(Color.LIGHTBLUE);

        Label dashDesc = new Label(
                "This area demonstrates StackPane, Pane, VBox, HBox, and GridPane.");
        dashDesc.setFont(Font.font("Arial",11));
        dashDesc.setTextFill(Color.BLACK);

        // HBox for Primary / Secondary buttons (BRIGHT YELLOW BORDER)
        HBox modeBox = new HBox(5);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.setBorder(new Border(new BorderStroke(
                Color.YELLOW, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3.5))));
        Button primaryBtn = new Button("Primary");
        primaryBtn.setStyle("-fx-background-color: #0abf70; -fx-text-fill: white;");
        Button secondaryBtn = new Button("Secondary");
        modeBox.getChildren().addAll(primaryBtn, secondaryBtn);

        // ===== GRIDPANE 3x3 FOR FORM (GREEN BORDER) =====
        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);
        formGrid.setPadding(new Insets(15));
        formGrid.setBorder(new Border(new BorderStroke(
                Color.GREEN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        formGrid.setStyle("-fx-background-color: white;");

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(20);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(55);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        formGrid.getColumnConstraints().addAll(col0, col1, col2);

        Label titleLbl = new Label("Title:");
        TextField titleField2 = new TextField();
        titleField2.setPromptText("Spans 2 columns");

        Label shortDescLbl = new Label("Short Description (spans two columns):");
        TextArea shortDescArea = new TextArea();
        shortDescArea.setPrefRowCount(2);

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Details (row 2, spans 2 columns)");
        detailsArea.setPrefRowCount(2);

        Button actionBtn = new Button("Action");
        actionBtn.setStyle("-fx-background-color: #9c27b0; -fx-text-fill: white;");

        // place controls
        formGrid.add(titleLbl, 0, 0);
        formGrid.add(titleField2, 1, 0, 2, 1);

        formGrid.add(shortDescLbl, 0, 1);
        formGrid.add(shortDescArea, 1, 1, 2, 1);

        formGrid.add(detailsArea, 1, 2);
        formGrid.add(actionBtn, 2, 2);

        dashBox.getChildren().addAll(dashTitle, dashDesc, modeBox, formGrid);

        // position dashBox in the center of centerPane
        dashBox.setLayoutX(20);
        dashBox.setLayoutY(40);

        grid.getChildren().add(dashBox);
        centerStack.getChildren().add(grid);

        // ===== RIGHT NOTES PANE (CYAN OUTLINE, SIMPLE PANE) =====
        VBox notesBox = new VBox(5);
        notesBox.setPadding(new Insets(10));
        notesBox.setPrefWidth(260);
        notesBox.setBorder(new Border(new BorderStroke(
                Color.CYAN, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        notesBox.setStyle("-fx-background-color: #ffffff;");

        Label notesLbl = new Label("Notes");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Write your notes here...");
        VBox.setVgrow(notesArea, Priority.ALWAYS);

        notesBox.getChildren().addAll(notesLbl, notesArea);

        // add three main sections to mainRow
        mainRow.getChildren().addAll(userBox, centerStack, notesBox);

        // ===== BOTTOM FLOWPANE BUTTON BAR (PURPLE OUTLINE) =====
        FlowPane bottomBar = new FlowPane(Orientation.HORIZONTAL);
        bottomBar.setHgap(8);
        bottomBar.setPadding(new Insets(8, 20, 8, 20));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setBorder(new Border(new BorderStroke(
                Color.MEDIUMPURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        bottomBar.setStyle("-fx-background-color: #f8f8f8;");

        Button clearBtn = new Button("Clear");
        clearBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

        bottomBar.getChildren().addAll(clearBtn, saveBtn, cancelBtn);

        AnchorPane.setLeftAnchor(bottomBar, 10.0);
        AnchorPane.setRightAnchor(bottomBar, 10.0);
        AnchorPane.setBottomAnchor(bottomBar, 10.0);
        anchorPane.getChildren().add(bottomBar);

        // ===== SHOW STAGE =====
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Assignment 3 Layout");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
