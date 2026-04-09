package Network;

import javax.xml.stream.EventFilter;
import java.io.Serializable;

public class Move implements Serializable {
    public int fromRow, fromCol;
    public int toRow, toCol;

    public Move(int fr, int fc, int tr, int tc) {
        fromRow = fr;
        fromCol = fc;
        toRow = tr;
        toCol = tc;
    }
}

