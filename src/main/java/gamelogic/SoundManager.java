package gamelogic;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private MediaPlayer backgroundPlayer;

    // ---------------- PUBLIC GAME SOUNDS ----------------

    public void playMove() {
        playOnce("file:src/main/resources/sounds/move.mp3");
    }

    public void playCapture() {
        playOnce("file:src/main/resources/sounds/piece captured.mp3");
    }

    public void playCheck() {
        playOnce("file:src/main/resources/sounds/chek to king.mp3");
    }

    public void playCheckMate() {
        playOnce("file:src/main/resources/sounds/checkmate.mp3");
    }

    public void playClick() {
        playOnce("file:src/main/resources/sounds/piece clicked.mp3");
    }

    public void startBackgroundMusic() {
        playLoop("file:src/main/resources/sounds/bg.mp3", 1.0);
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

    private void playOnce(String filePath) {
        Media media = new Media(filePath);
        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(1.0);
        player.setOnEndOfMedia(player::dispose);
        player.play();
    }

    private void playLoop(String filePath, double volume) {
        stopBackgroundMusic();
        Media media = new Media(filePath);
        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setVolume(volume);
        backgroundPlayer.play();
    }
}
