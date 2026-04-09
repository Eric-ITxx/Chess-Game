package makingstats;
import java.util.ArrayList;
import java.util.List;

public class Players {
    private static List<PlayerData> players = new ArrayList<>();


    public static void addPlayer(String name, int rating) {
        for (PlayerData p : players) {
            if (p.name.equals(name))
                return;
        }
        players.add(new PlayerData(name, rating, 0));
    }

    public static void updatePlayer(String name, int rating, Boolean win) {
        for (PlayerData p : players) {
            if (p.name.equals(name)) {
                p.rating = rating;
                if (win != null && win)
                    p.wins++;
                return;
            }
        }
        addPlayer(name, rating);
    }

    public static List<PlayerData> getPlayers() {
        return players;
    }

    public static class PlayerData {
        String name;
        int rating;
        int wins;

        PlayerData(String name, int rating, int wins) {
            this.name = name;
            this.rating = rating;
            this.wins = wins;
        }
        public String getName() {
            return name;
        }
        public int getRating() {
            return rating;
        }
        public int getWins() {
            return wins;
        }
    }
}
