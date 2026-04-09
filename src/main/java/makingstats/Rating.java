package makingstats;
public class Rating {
    private String playerName;
    private int rating;
    private static final int DEFAULT_RATING = 800;
    private static final int WIN_POINTS = 25;
    private static final int DRAW_POINTS = 10;
    private static final int LOSS_POINTS = 15;

    public Rating(String playerName) {
        this.playerName = playerName;
        this.rating = DEFAULT_RATING;
        Players.addPlayer(playerName, rating);
        LeaderBoard.updateBoard();
    }

    public Rating(String playerName, int rating) {
        this.playerName = playerName;
        this.rating = rating;
        Players.addPlayer(playerName, rating);
        LeaderBoard.updateBoard();
    }

    public void win() {
        rating += WIN_POINTS;
        Players.updatePlayer(playerName, rating, true);
        LeaderBoard.updateBoard();
    }

    public void lose() {
        rating -= LOSS_POINTS;
        if (rating < 0) rating = 0;
        Players.updatePlayer(playerName, rating, false);
        LeaderBoard.updateBoard();
    }

    public void draw() {
        rating += DRAW_POINTS;
        Players.updatePlayer(playerName, rating, null);
        LeaderBoard.updateBoard();
    }

    public int getRating() {
        return rating;
    }

    public int compareTo(Rating other) {
        return this.rating - other.rating;
    }

    public void printRating() {
        System.out.println(playerName + " has a rating of " + rating);
    }
}
