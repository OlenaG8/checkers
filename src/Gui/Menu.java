package Gui;

import Communication.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Menu extends JFrame {

    private BufferedImage backgroundImage;

    public Menu() {
        setTitle("Menu Główne");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        try {
            backgroundImage = ImageIO.read(new File("images/menu-background.jpg"));
        } catch (IOException e) {
            System.out.println("Błąd ładowania obrazka tła menu: " + e.getMessage());
        }

        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

                    g.setColor(new Color(0, 0, 0, 150));
                    g.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    setBackground(new Color(51, 49, 43));
                }
            }
        };

        mainPanel.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel("Warcaby");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        JButton localPlayBtn = createMenuButton("Graj lokalnie");
        localPlayBtn.addActionListener(e -> startLocalGame());
        mainPanel.add(localPlayBtn, gbc);

        gbc.gridy++;
        JButton aiPlayBtn = createMenuButton("Graj z komputerem");
        aiPlayBtn.addActionListener(e -> showNotImplementedMessage("Gra z komputerem"));
        mainPanel.add(aiPlayBtn, gbc);

        gbc.gridy++;
        JButton onlinePlayBtn = createMenuButton("Graj w sieci");
        onlinePlayBtn.addActionListener(e -> showNotImplementedMessage("Gra online"));
        mainPanel.add(onlinePlayBtn, gbc);

        gbc.gridy++;
        JButton exitBtn = createMenuButton("Wyjście");
        exitBtn.addActionListener(e -> System.exit(0));
        mainPanel.add(exitBtn, gbc);

        add(mainPanel);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            private final Color normalColor = new Color(136, 132, 132, 190);
            private final Color hoverColor = new Color(182, 180, 180, 250);
            private Color currentColor = normalColor;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);

                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        currentColor = hoverColor;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        currentColor = normalColor;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(currentColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2d.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void startLocalGame() {
        Client client;
        try {
            client = new Client("127.0.0.1");
        } catch (IOException e) {
            System.out.println("Unable to connect to server: " + e.getMessage());
            return;
        }

        this.dispose();
        SwingUtilities.invokeLater(() -> {
            Gui gameWindow = new Gui(client);
            gameWindow.setVisible(true);
        });
    }

    private void showNotImplementedMessage(String featureName) {
        JOptionPane.showMessageDialog(this,
                featureName + " nie jest jeszcze zaimplementowana.\nBędzie dostępna wkrótce!",
                "W budowie",
                JOptionPane.INFORMATION_MESSAGE);
    }
}