package Frontend;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Two-player countdown chess clock.
 * Call startWhite() / startBlack() to hand off time after each move.
 */
public class ChessClock {

    private long whiteSeconds;
    private long blackSeconds;
    private boolean whiteTurn = true;
    private boolean stopped   = false;

    private final Timeline  timeline;
    private final Text      whiteLabel;
    private final Text      blackLabel;
    private final Runnable  onWhiteTimeout;
    private final Runnable  onBlackTimeout;

    /**
     * @param secondsEach   starting time for each player in seconds (0 = no limit)
     * @param onWhiteTimeout called on the FX thread when white's time hits zero
     * @param onBlackTimeout called on the FX thread when black's time hits zero
     */
    public ChessClock(long secondsEach, Runnable onWhiteTimeout, Runnable onBlackTimeout) {
        this.whiteSeconds   = secondsEach;
        this.blackSeconds   = secondsEach;
        this.onWhiteTimeout = onWhiteTimeout;
        this.onBlackTimeout = onBlackTimeout;

        whiteLabel = makeLabel();
        blackLabel = makeLabel();
        updateLabels();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    // ── public API ──────────────────────────────────────────────────────────

    /** Start (or hand off to) white's clock. */
    public void startWhite() {
        whiteTurn = true;
        if (!stopped) timeline.play();
    }

    /** Start (or hand off to) black's clock. */
    public void startBlack() {
        whiteTurn = false;
        if (!stopped) timeline.play();
    }

    public void pause()  { timeline.pause(); }
    public void resume() { if (!stopped) timeline.play(); }
    public void stop()   { stopped = true; timeline.stop(); }

    public Text getWhiteLabel() { return whiteLabel; }
    public Text getBlackLabel() { return blackLabel; }

    // ── internals ───────────────────────────────────────────────────────────

    private void tick() {
        if (stopped) return;
        if (whiteTurn) {
            whiteSeconds--;
            if (whiteSeconds <= 0) { whiteSeconds = 0; updateLabels(); stop(); onWhiteTimeout.run(); return; }
        } else {
            blackSeconds--;
            if (blackSeconds <= 0) { blackSeconds = 0; updateLabels(); stop(); onBlackTimeout.run(); return; }
        }
        updateLabels();
    }

    private void updateLabels() {
        whiteLabel.setText("White  " + fmt(whiteSeconds));
        blackLabel.setText("Black  " + fmt(blackSeconds));
        whiteLabel.setFill(whiteSeconds <= 10 ? Color.RED : Color.WHITE);
        blackLabel.setFill(blackSeconds <= 10 ? Color.RED : Color.WHITE);
    }

    private static String fmt(long secs) {
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    private static Text makeLabel() {
        Text t = new Text();
        t.setFont(Font.font("Monospaced", 18));
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font-weight: bold;");
        return t;
    }
}
