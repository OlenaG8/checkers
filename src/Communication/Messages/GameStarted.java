package Communication.Messages;

import Logic.PlayerColor;

import java.io.Serial;
import java.io.Serializable;

public class GameStarted implements Serializable {
    @Serial
    private static final long serialVersionUID = 42L;

    private final PlayerColor yourColor;

    public GameStarted(PlayerColor yourColor) {
        this.yourColor = yourColor;
    }

    public PlayerColor getYourColor() {
        return yourColor;
    }

    @Override
    public String toString() {
        return "yourColor=" + yourColor;
    }
}
