package Frontend;

import gamelogic.Color;
import gamelogic.piecetype;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.VPos;

/**
 * Renders chess pieces entirely with JavaFX shapes — no image files needed.
 *
 * Each piece is a StackPane containing:
 *   1. A radial-gradient circle background  (ivory for white, walnut for black)
 *   2. A filled chess unicode symbol        (♚ ♛ ♜ ♝ ♞ ♟)
 *   3. Drop shadow for depth
 */
public class PieceRenderer {

    /**
     * Creates a smooth, vector-rendered chess piece node.
     *
     * @param type   piece type
     * @param color  piece color (gamelogic.Color.WHITE / BLACK)
     * @param size   cell size in pixels — the returned StackPane will be size×size
     * @return       a StackPane tagged with id="pieceNode"
     */
    public static StackPane createPiece(piecetype type, Color color, int size) {
        boolean isWhite = (color == Color.WHITE);
        double radius   = size * 0.41;

        // ── Background circle ──────────────────────────────────────────────
        Circle bg = new Circle(radius);

        if (isWhite) {
            // Deep slate-blue — bright cream symbol pops against it clearly
            bg.setFill(new RadialGradient(
                0, 0, 0.38, 0.32, 0.68, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, javafx.scene.paint.Color.rgb(75, 90, 120)),
                new Stop(0.6, javafx.scene.paint.Color.rgb(45, 58, 85)),
                new Stop(1.0, javafx.scene.paint.Color.rgb(20, 28, 50))
            ));
            bg.setStroke(javafx.scene.paint.Color.rgb(160, 175, 210));
        } else {
            bg.setFill(new RadialGradient(
                0, 0, 0.38, 0.32, 0.68, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, javafx.scene.paint.Color.rgb(90, 72, 55)),
                new Stop(0.6, javafx.scene.paint.Color.rgb(48, 37, 26)),
                new Stop(1.0, javafx.scene.paint.Color.rgb(18, 12, 6))
            ));
            bg.setStroke(javafx.scene.paint.Color.rgb(160, 135, 90));
        }
        bg.setStrokeWidth(1.8);

        // ── Drop shadow ────────────────────────────────────────────────────
        DropShadow outerShadow = new DropShadow();
        outerShadow.setRadius(10);
        outerShadow.setOffsetX(2);
        outerShadow.setOffsetY(4);
        outerShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.85));
        bg.setEffect(outerShadow);

        // ── Chess unicode symbol ──────────────────────────────────────────
        Text sym = new Text(getSymbol(type));
        sym.setFont(Font.font("Segoe UI Symbol", size * 0.50));
        sym.setTextOrigin(VPos.CENTER);

        if (isWhite) {
            sym.setFill(javafx.scene.paint.Color.rgb(248, 238, 215));
            // Subtle inner glow so white symbols pop on the light circle
            InnerShadow glow = new InnerShadow();
            glow.setRadius(3);
            glow.setColor(javafx.scene.paint.Color.rgb(255, 240, 150, 0.6));
            sym.setEffect(glow);
        } else {
            sym.setFill(javafx.scene.paint.Color.rgb(22, 14, 6));
        }

        // ── Assemble ──────────────────────────────────────────────────────
        StackPane pane = new StackPane(bg, sym);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        pane.setId("pieceNode");
        return pane;
    }

    // Always use the filled (black) unicode variants — visual distinction
    // comes from the background gradient, not the symbol outline.
    private static String getSymbol(piecetype type) {
        return switch (type) {
            case KING   -> "♚";
            case QUEEN  -> "♛";
            case ROOK   -> "♜";
            case BISHOP -> "♝";
            case KNIGHT -> "♞";
            case PAWN   -> "♟";
        };
    }
}
