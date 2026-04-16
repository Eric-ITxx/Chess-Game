package Frontend;

import javafx.scene.paint.Color;

/**
 * Singleton-style class holding global game settings (theme, etc.)
 * so they persist across screens without needing a database.
 */
public class GameSettings {

    private static Color lightSq     = Color.rgb(180, 188, 196);  // default: Dark theme
    private static Color darkSq      = Color.rgb(34,  34,  34);
    private static String currentTheme = "Dark";

    private GameSettings() {}

    public static void setTheme(String theme) {
        currentTheme = theme;
        switch (theme) {
            case "Classic" -> { lightSq = Color.rgb(240, 217, 181); darkSq = Color.rgb(181, 136,  99); }
            case "Wood"    -> { lightSq = Color.rgb(222, 184, 135); darkSq = Color.rgb(101,  67,  33); }
            case "Marble"  -> { lightSq = Color.rgb(230, 230, 230); darkSq = Color.rgb( 90,  90,  90); }
            case "Modern"  -> { lightSq = Color.rgb(100, 149, 237); darkSq = Color.rgb( 25,  25, 112); }
            default        -> { lightSq = Color.rgb(180, 188, 196); darkSq = Color.rgb( 34,  34,  34); } // Dark
        }
    }

    public static Color getLightSq()      { return lightSq;      }
    public static Color getDarkSq()       { return darkSq;       }
    public static String getCurrentTheme(){ return currentTheme; }
}
