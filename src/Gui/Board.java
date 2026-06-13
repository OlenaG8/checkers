package Gui;

import Logic.MovementLogic;
import Logic.PieceType;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Board extends JPanel {
    public static final int NUM_OF_TILES = 8;

    private BufferedImage boardImage;
    private BufferedImage whitePieceImage;
    private BufferedImage blackPieceImage;
    private BufferedImage whiteQueenImage;
    private BufferedImage blackQueenImage;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isJumpingSequence = false;

    private final Gui parentGUI;
    private final MovementLogic logic;

    public Board(Gui parentGUI, MovementLogic logic) {
        this.parentGUI = parentGUI;
        setBackground(new Color(51, 49, 43));
        this.logic = logic;

        try {
            boardImage = ImageIO.read(new File("images/chessboard.png"));
            whitePieceImage = ImageIO.read(new File("images/vanillaPiece.png"));
            blackPieceImage = ImageIO.read(new File("images/chocolatePiece.png"));
            whiteQueenImage = ImageIO.read(new File("images/vanillaQueen.png"));
            blackQueenImage = ImageIO.read(new File("images/chocolateQueen.png"));
        } catch (IOException e) {
            System.out.println("Nie udało się załadować rafik: " + e.getMessage());
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int tileWidth = getWidth() / NUM_OF_TILES;
                int tileHeight = getHeight() / NUM_OF_TILES;

                int col = e.getX() / tileWidth;
                int row = e.getY() / tileHeight;

                if (col < 0 || col > NUM_OF_TILES - 1 || row < 0 || row > NUM_OF_TILES - 1) return;

                if (isJumpingSequence) {
                    int moveResult = logic.moveCheckerOnce(selectedRow, selectedCol, row, col);
                    if (moveResult == 2) {
                        if (logic.hasAnyJumps(row, col)) {
                            selectedRow = row;
                            selectedCol = col;
                        } else {
                            endTurnLocal();
                        }
                    }
                } else {
                    if (selectedRow == -1) {
                        if (isCurrentPlayerPiece(logic.boardState[row][col])) {
                            selectedRow = row;
                            selectedCol = col;
                        }
                    } else {
                        if (row == selectedRow && col == selectedCol) {
                            selectedRow = -1;
                            selectedCol = -1;
                        } else {
                            int moveResult = logic.moveCheckerOnce(selectedRow, selectedCol, row, col);
                            if (moveResult == 1) {
                                endTurnLocal();
                            } else if (moveResult == 2) {
                                if (logic.hasAnyJumps(row, col)) {
                                    selectedRow = row;
                                    selectedCol = col;
                                    isJumpingSequence = true;
                                } else {
                                    endTurnLocal();
                                }
                            } else {
                                if (isCurrentPlayerPiece(logic.boardState[row][col])) {
                                    selectedRow = row;
                                    selectedCol = col;
                                } else {
                                    selectedRow = -1;
                                    selectedCol = -1;
                                }
                            }
                        }
                    }
                }
                repaint();
            }
        });
    }

    private void endTurnLocal() {
        selectedRow = -1;
        selectedCol = -1;
        isJumpingSequence = false;
        parentGUI.switchPlayer();

        parentGUI.checkWinCondition();
    }

    private boolean isCurrentPlayerPiece(PieceType pieceType) {
        if (pieceType == null) return false;
        int currentPlayer = parentGUI.getCurrentPlayer();
        if (currentPlayer == 1) return pieceType.isVanilla();
        if (currentPlayer == 2) return !pieceType.isVanilla();
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if (boardImage != null) {
            g2d.drawImage(boardImage, 0, 0, panelWidth, panelHeight, this);
        }

        double tileWidth = (double) panelWidth / NUM_OF_TILES;
        double tileHeight = (double) panelHeight / NUM_OF_TILES;

        double pieceToTileRatio = 0.9;
        double pieceWidth = tileWidth * pieceToTileRatio;
        double pieceHeight = tileHeight * pieceToTileRatio;

        if (selectedRow != -1 && selectedCol != -1) {
            double tileCenterX = (selectedCol * tileWidth) + (tileWidth / 2.0);
            double tileCenterY = (selectedRow * tileHeight) + (tileHeight / 2.0);

            double highlightToTileRatio = 0.95;
            double highlightWidth = tileWidth * highlightToTileRatio;
            double highlightHeight = tileHeight * highlightToTileRatio;

            int hX = (int) Math.round(tileCenterX - (highlightWidth / 2.0));
            int hY = (int) Math.round(tileCenterY - (highlightHeight / 2.0));

            if (isJumpingSequence) {
                g2d.setColor(new Color(255, 0, 0, 80));
            } else {
                g2d.setColor(new Color(0, 255, 0, 80));
            }
            g2d.fillOval(hX, hY, (int) Math.round(highlightWidth), (int) Math.round(highlightHeight));

            g2d.setColor(new Color(174, 174, 174, 180));
            double moveHighlightRatio = 0.2;
            double moveHighlightWidth = tileWidth * moveHighlightRatio;
            double moveHighlightHeight = tileHeight * moveHighlightRatio;

            for (int r = 0; r < NUM_OF_TILES; r++) {
                for (int c = 0; c < NUM_OF_TILES; c++) {
                    int moveType = logic.canCheckerMoveOnce(selectedRow, selectedCol, r, c);
                    if (moveType > 0) {
                        if (isJumpingSequence && moveType != 2) continue;

                        double targetCenterX = (c * tileWidth) + (tileWidth / 2.0);
                        double targetCenterY = (r * tileHeight) + (tileHeight / 2.0);

                        int thX = (int) Math.round(targetCenterX - (moveHighlightWidth / 2.0));
                        int thY = (int) Math.round(targetCenterY - (moveHighlightHeight / 2.0));

                        g2d.fillOval(thX, thY, (int) Math.round(moveHighlightWidth), (int) Math.round(moveHighlightHeight));
                    }
                }
            }
        }

        for (int row = 0; row < NUM_OF_TILES; row++) {
            for (int col = 0; col < NUM_OF_TILES; col++) {
                PieceType pieceType = logic.boardState[row][col];

                if (pieceType != null) {
                    double tileCenterX = (col * tileWidth) + (tileWidth / 2.0);
                    double tileCenterY = (row * tileHeight) + (tileHeight / 2.0);

                    int x = (int) Math.round(tileCenterX - (pieceWidth / 2.0));
                    int y = (int) Math.round(tileCenterY - (pieceHeight / 2.0));
                    int w = (int) Math.round(pieceWidth);
                    int h = (int) Math.round(pieceHeight);

                    if (pieceType == PieceType.VANILLA && whitePieceImage != null) {
                        g2d.drawImage(whitePieceImage, x, y, w, h, this);
                    } else if (pieceType == PieceType.CHOCOLATE && blackPieceImage != null) {
                        g2d.drawImage(blackPieceImage, x, y, w, h, this);
                    } else if (pieceType == PieceType.VANILLA_QUEEN && whiteQueenImage != null) {
                        g2d.drawImage(whiteQueenImage, x, y, w, h, this);
                    } else if (pieceType == PieceType.CHOCOLATE_QUEEN && blackQueenImage != null) {
                        g2d.drawImage(blackQueenImage, x, y, w, h, this);
                    }
                }
            }
        }
    }
}