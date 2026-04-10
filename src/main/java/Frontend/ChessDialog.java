package Frontend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Dark-themed dialogs that match the chess game's visual style.
 * Drop-in replacement for ugly default Java Alert boxes.
 */
public class ChessDialog {

    private static final String DARK_BG  = "#0d1117";
    private static final String CARD_BG  = "#161b22";
    private static final String BORDER   = "#30363d";
    private static final String TEXT_FG  = "#e6edf3";
    private static final String MUTED_FG = "#8b949e";
    private static final String BTN_BG   = "#21262d";
    private static final String BTN_HVR  = "#30363d";
    private static final String ACCENT   = "#388bfd";

    // ─────────────────────────────────────────────────────────────
    //  Info dialog — just an OK button
    // ─────────────────────────────────────────────────────────────
    public static void showInfo(Stage owner, String title, String message) {
        show(owner, title, message, "OK");
    }

    // ─────────────────────────────────────────────────────────────
    //  Confirm dialog — returns the button label the user clicked
    // ─────────────────────────────────────────────────────────────
    public static String showConfirm(Stage owner, String title, String message, String... buttons) {
        return show(owner, title, message, buttons);
    }

    // ─────────────────────────────────────────────────────────────
    //  Core builder
    // ─────────────────────────────────────────────────────────────
    private static String show(Stage owner, String title, String message, String... buttonLabels) {
        AtomicReference<String> result = new AtomicReference<>(
                buttonLabels.length > 0 ? buttonLabels[0] : "OK");

        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setResizable(false);

        // ── outer drop shadow shell ──
        VBox shell = new VBox();
        shell.setStyle("-fx-background-color: transparent;");
        shell.setPadding(new Insets(16));

        // ── card ──
        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28, 32, 24, 32));
        card.setMinWidth(380);
        card.setMaxWidth(440);
        card.setStyle(
            "-fx-background-color: " + CARD_BG + ";" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.7));
        shadow.setRadius(30);
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        // ── title ──
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-text-fill: " + TEXT_FG + ";" +
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;"
        );
        titleLabel.setWrapText(true);

        // ── separator line ──
        Rectangle sep = new Rectangle(320, 1);
        sep.setFill(Color.web(BORDER));

        // ── message ──
        Label msgLabel = new Label(message);
        msgLabel.setStyle(
            "-fx-text-fill: " + MUTED_FG + ";" +
            "-fx-font-size: 14px;" +
            "-fx-line-spacing: 4;"
        );
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(380);
        msgLabel.setAlignment(Pos.CENTER);

        // ── buttons ──
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            boolean isPrimary = (i == buttonLabels.length - 1);

            Button btn = new Button(label);
            btn.setPrefWidth(120);
            btn.setPrefHeight(40);

            String baseBg  = isPrimary ? ACCENT   : BTN_BG;
            String hoverBg = isPrimary ? "#1f6feb" : BTN_HVR;

            btn.setStyle(buildBtnStyle(baseBg));
            btn.setOnMouseEntered(e -> btn.setStyle(buildBtnStyle(hoverBg)));
            btn.setOnMouseExited (e -> btn.setStyle(buildBtnStyle(baseBg)));

            btn.setOnAction(e -> {
                result.set(label);
                dialog.close();
            });
            btnRow.getChildren().add(btn);
        }

        card.getChildren().addAll(titleLabel, sep, msgLabel, btnRow);
        shell.getChildren().add(card);

        // ── scene with transparent fill ──
        Scene scene = new Scene(shell);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result.get();
    }

    private static String buildBtnStyle(String bg) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-text-fill: " + TEXT_FG + ";" +
               "-fx-font-size: 13px;" +
               "-fx-font-weight: bold;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;";
    }
}
