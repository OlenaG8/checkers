import Communication.Client;
import Gui.Menu;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    private static final int PORT = 7777;
    private static final String IP = "127.0.0.1";

    public static void main() throws IOException {
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.setVisible(true);
        });
    }
}