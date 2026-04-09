package makingstats;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderBoard {
    public static void updateBoard() {
        List<Players.PlayerData> players = Players.getPlayers();
        int n = players.size();

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Players.PlayerData p1 = players.get(j);
                Players.PlayerData p2 = players.get(j + 1);

                // Compare by rating first, then by wins
                if (p1.rating < p2.rating ||
                        (p1.rating == p2.rating && p1.wins < p2.wins)) {

                    // Swap positions
                    players.set(j, p2);
                    players.set(j + 1, p1);
                }
            }
        }
    }


    public static void printBoard() {
        List<Players.PlayerData> players = Players.getPlayers();
        System.out.println("=== Leaderboard ===");
        int rank = 1;
        for (Players.PlayerData p : players) {
            System.out.println(rank + ". " + p.name + " - Rating: " + p.rating + " | Wins: " + p.wins);
            rank++;
        }
    }
}
