package Network;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class GUIclient {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String ip, int port, Consumer<Move> onMoveReceived) {
        new Thread(() -> {
            try {
                Socket s = new Socket(ip, port);

                out = new ObjectOutputStream(s.getOutputStream());
                in  = new ObjectInputStream(s.getInputStream());

                while (true) {
                    Move m = (Move) in.readObject();
                    onMoveReceived.accept(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMove(Move m) {
        try {
            out.writeObject(m);
            out.flush();
        } catch (Exception ignored) {}
    }
}
