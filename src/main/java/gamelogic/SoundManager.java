package gamelogic;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private MediaPlayer backgroundPlayer;

    // ---------------- PUBLIC GAME SOUNDS ----------------

    public void playMove() {
        playOnce("/sounds/move.mp3");
    }

    public void playCapture() {
        playOnce("/sounds/piece captured.mp3");
    }

    public void playCheck() {
        playOnce("/sounds/chek to king.mp3");
    }

    public void playCheckMate() {
        playOnce("/sounds/checkmate.mp3");
    }

    public void playClick() {
        playOnce("/sounds/piece clicked.mp3");
    }

    public void startBackgroundMusic() {
        playLoop("/sounds/bg.mp3", 1.0);
    }

    public void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
            backgroundPlayer = null;
        }
    }

    public void setBackgroundVolume(double volume) {
        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(volume);
        }
    }

    // ---------------- INTERNAL LOGIC ----------------

    private void playOnce(String classpathPath) {
        try {
            var url = getClass().getResource(classpathPath);
            if (url == null) return;
            Media media = new Media(url.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(1.0);
            player.setOnEndOfMedia(player::dispose);
            player.play();
        } catch (Exception ignored) {}
    }

    private void playLoop(String classpathPath, double volume) {
        stopBackgroundMusic();
        try {
            var url = getClass().getResource(classpathPath);
            if (url == null) return;
            Media media = new Media(url.toExternalForm());
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.setVolume(volume);
            backgroundPlayer.play();
        } catch (Exception ignored) {}
    }
}
