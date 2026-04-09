package gamelogic;
public class Playerturn {
    private Color currentTurn; // Color.WHITE or Color.BLACK


    public Playerturn(Color startingColor) {
        this.currentTurn = startingColor;
    }

    public void startTurn() {
        System.out.println(currentTurn + "'s turn started.");
    }
    public void setCurrentTurn(Color currentTurn) {
        this.currentTurn = currentTurn;
    }
    public void endTurn() {
        System.out.println(currentTurn + "'s turn ended.");
        switchTurn();
    }

    private void switchTurn() {
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }
}

