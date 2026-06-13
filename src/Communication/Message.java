package Communication;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

public class Message implements Serializable {
    private final String message;
    private final Instant alertTime;

    @Serial
    private static final long serialVersionUID = 42L;

    public Message(String message, Instant alertTime) {
        this.message = message;
        this.alertTime = alertTime;
    }

    public String getMessage() {
        return message;
    }

    public Instant getAlertTime() {
        return alertTime;
    }

}
