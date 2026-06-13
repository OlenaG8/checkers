package Communication.Messages;

import java.io.Serial;
import java.io.Serializable;

public class Move implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    private final Position from;
    private final Position to;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    @Override
    public String toString() {
        return from + " -> " + to;
    }
}
