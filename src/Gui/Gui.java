package Gui;

import Communication.Client;
import Logic.GameState;
import Logic.PlayerColor;

import javax.swing.*;
import java.awt.*;

public class Gui extends JFrame {
    private final JLabel whiteTurnLabel;
    private final JLabel blackTurnLabel;
    private final JLabel whiteTimeLabel;
    private final JLabel blackTimeLabel;

    private Timer timer;
    private int whiteSeconds = 0;
    private int blackSeconds = 0;

    private GameState state = new GameState(GameState.StartPosition.VANILLA_ON_BOTTOM);

    public Gui(Client client) {
        setTitle("Warcaby");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(51, 49, 43));

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Opcje");
        JMenuItem returnItem = new JMenuItem("Wróć do Menu");
        returnItem.addActionListener(_ -> returnToMenu());
        gameMenu.add(returnItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        JPanel topPanel = createPlayerPanel("Czekoladowe");
        blackTurnLabel = (JLabel) topPanel.getComponent(0);
        blackTimeLabel = (JLabel) topPanel.getComponent(1);

        JPanel bottomPanel = createPlayerPanel("Waniliowe (Twój ruch)");
        whiteTurnLabel = (JLabel) bottomPanel.getComponent(0);
        whiteTimeLabel = (JLabel) bottomPanel.getComponent(1);

        add(topPanel, BorderLayout.NORTH);

        JPanel boardWrapper = new JPanel() {
            @Override
            public void doLayout() {
                if (getComponentCount() > 0) {
                    Component board = getComponent(0);
                    int size = Math.min(getWidth(), getHeight());

                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    board.setBounds(x, y, size, size);
                }
            }
        };
        boardWrapper.add(new Board(this, client, state));
        boardWrapper.setBackground(new Color(51, 49, 43));
        add(boardWrapper, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        startTimer();
    }

    private JPanel createPlayerPanel(String defaultText) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(51, 49, 43));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel turnLabel = new JLabel(defaultText, SwingConstants.LEFT);
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        turnLabel.setForeground(Color.WHITE);

        JLabel timeLabel = new JLabel("00:00", SwingConstants.RIGHT);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        timeLabel.setForeground(Color.WHITE);

        panel.add(turnLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);
        return panel;
    }

    private void startTimer() {
        timer = new Timer(1000, _ -> {
            if (state.currentPlayer == PlayerColor.VANILLA) {
                whiteSeconds++;
                whiteTimeLabel.setText(String.format("%02d:%02d", whiteSeconds / 60, whiteSeconds % 60));
            } else {
                blackSeconds++;
                blackTimeLabel.setText(String.format("%02d:%02d", blackSeconds / 60, blackSeconds % 60));
            }
        });
        timer.start();
    }

    public void switchPlayer() {
        state.switchPlayer();

        if (state.currentPlayer == PlayerColor.VANILLA) {
            whiteTurnLabel.setText("Waniliowe (Twój ruch)");
            blackTurnLabel.setText("Czekoladowe");
        } else {
            whiteTurnLabel.setText("Waniliowe");
            blackTurnLabel.setText("Czekoladowe (Twój ruch)");
        }
    }

    public void handleWinCondition() {
        if (timer != null) timer.stop();

        Timer delayTimer = new Timer(300, _ -> {
            PlayerColor winner = state.currentPlayer == PlayerColor.VANILLA ? PlayerColor.CHOCOLATE : PlayerColor.VANILLA;
            String winnerName = winner == PlayerColor.VANILLA ? "Waniliowe" : "Czekoladowe";

            Object[] options = {"Powrót do Menu", "Opuść grę"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Wygrywają " + winnerName + "!\nGratulacje!",
                    "Podsumowanie",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) {
                returnToMenu();
            } else {
                System.exit(0);
            }
        });

        delayTimer.setRepeats(false);
        delayTimer.start();
    }

    public void returnToMenu() {
        if (timer != null) timer.stop();
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            Menu menu = new Menu();
            menu.setVisible(true);
        });
    }
}