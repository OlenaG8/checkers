package Gui;

import Communication.Client;
import Communication.Messages.Position;
import Logic.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Board extends JPanel {
    public static final int NUM_OF_TILES = 8;

    private BufferedImage boardImage;
    private BufferedImage whitePieceImage;
    private BufferedImage blackPieceImage;
    private BufferedImage whiteQueenImage;
    private BufferedImage blackQueenImage;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private final Gui parentGUI;
    private final Client client;
    private final GameState state;
    private final PlayerColor myColor;
    private final boolean boardFlipped;

    private List<Position> allowedMoves = Collections.emptyList();

    public Board(Gui parentGUI, Client client, GameState state, PlayerColor myColor) {
        this.parentGUI = parentGUI;
        this.client = client;
        this.state = state;
        this.myColor = myColor;
        this.boardFlipped = myColor == PlayerColor.CHOCOLATE;

        setBackground(new Color(51, 49, 43));
        setPreferredSize(new Dimension(850, 850));

        try {
            boardImage = ImageIO.read(new File("images/chessboard.png"));
            whitePieceImage = ImageIO.read(new File("images/vanillaPiece.png"));
            blackPieceImage = ImageIO.read(new File("images/chocolatePiece.png"));
            whiteQueenImage = ImageIO.read(new File("images/vanillaQueen.png"));
            blackQueenImage = ImageIO.read(new File("images/chocolateQueen.png"));
        } catch (IOException e) {
            System.out.println("Nie udało się załadować grafik: " + e.getMessage());
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state.currentPlayer != myColor) {
                    return;
                }

                int tileWidth = getWidth() / NUM_OF_TILES;
                int tileHeight = getHeight() / NUM_OF_TILES;

                int col = e.getX() / tileWidth;
                int row = e.getY() / tileHeight;

                if (boardFlipped) {
                    col = NUM_OF_TILES - 1 - col;
                    row = NUM_OF_TILES - 1 - row;
                }

                if (col < 0 || col > NUM_OF_TILES - 1 || row < 0 || row > NUM_OF_TILES - 1) return;

                if (state.isJumpingSequence) {
                    int moveResult = MovementLogic.canMove(state, selectedRow, selectedCol, row, col);
                    if (moveResult > 0) {
                        client.move(selectedRow, selectedCol, row, col);
                        MoveResult res = state.move(selectedRow, selectedCol, row, col);
                        switch (res) {
                            case END_TURN:
                                endTurnLocal();
                                break;
                            case ENTER_JUMP_SEQUENCE:
                                selectPiece(row, col);
                                break;
                        }
                    }
                } else {
                    if (selectedRow == -1) {
                        if (isCurrentPlayerPiece(state.board[row][col])) {
                            selectPiece(row, col);
                        }
                    } else {
                        if (row == selectedRow && col == selectedCol) {
                            unselectPiece();
                        } else {
                            int moveResult = MovementLogic.canMove(state, selectedRow, selectedCol, row, col);
                            if (moveResult == 0) {
                                if (isCurrentPlayerPiece(state.board[row][col])) {
                                    selectPiece(row, col);
                                } else {
                                    unselectPiece();
                                }
                            } else {
                                client.move(selectedRow, selectedCol, row, col);
                                MoveResult res = state.move(selectedRow, selectedCol, row, col);
                                switch (res) {
                                    case END_TURN:
                                        endTurnLocal();
                                        break;
                                    case ENTER_JUMP_SEQUENCE:
                                        selectPiece(row, col);
                                        break;
                                }
                            }
                        }
                    }
                }
                repaint();
            }
        });

        client.onMoveResult(res -> {
            if (res == MoveResult.INVALID_MOVE) {
                throw new IllegalStateException("Unexpected move result: " + res);
            }
        });

        client.onOpponentMove(move -> {
            MoveResult res = state.move(
                    move.getFrom().getRow(),
                    move.getFrom().getCol(),
                    move.getTo().getRow(),
                    move.getTo().getCol()
            );
            switch (res) {
                case END_TURN:
                    endTurnLocal();
                    break;
                case ENTER_JUMP_SEQUENCE:
                    selectPiece(move.getTo().getRow(), move.getTo().getCol());
                    break;
            }
            repaint();
        });
    }

    private void unselectPiece() {
        selectedRow = -1;
        selectedCol = -1;
        allowedMoves = Collections.emptyList();
    }

    private void selectPiece(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        allowedMoves = MovementLogic.getAllowedMoves(state, row, col);
    }

    private void endTurnLocal() {
        unselectPiece();
        state.isJumpingSequence = false;

        parentGUI.switchPlayer();
        if (!MovementLogic.hasAnyValidMoves(state, state.currentPlayer)) {
            parentGUI.handleWinCondition();
        }
    }

    private boolean isCurrentPlayerPiece(PieceType pieceType) {
        if (pieceType == null) return false;
        return pieceType.matches(state.currentPlayer);
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
            int col = selectedCol;
            int row = selectedRow;

            if (boardFlipped) {
                col = NUM_OF_TILES - col - 1;
                row = NUM_OF_TILES - row - 1;
            }

            double tileCenterX = (col * tileWidth) + (tileWidth / 2.0);
            double tileCenterY = (row * tileHeight) + (tileHeight / 2.0);

            double highlightToTileRatio = 0.95;
            double highlightWidth = tileWidth * highlightToTileRatio;
            double highlightHeight = tileHeight * highlightToTileRatio;

            int hX = (int) Math.round(tileCenterX - (highlightWidth / 2.0));
            int hY = (int) Math.round(tileCenterY - (highlightHeight / 2.0));

            if (state.isJumpingSequence) {
                g2d.setColor(new Color(255, 0, 0, 80));
            } else {
                g2d.setColor(new Color(0, 255, 0, 80));
            }
            g2d.fillOval(hX, hY, (int) Math.round(highlightWidth), (int) Math.round(highlightHeight));

            g2d.setColor(new Color(174, 174, 174, 180));
            double moveHighlightRatio = 0.2;
            double moveHighlightWidth = tileWidth * moveHighlightRatio;
            double moveHighlightHeight = tileHeight * moveHighlightRatio;

            for (Position move : allowedMoves) {
                col = move.getCol();
                row = move.getRow();

                if (boardFlipped) {
                    col = NUM_OF_TILES - col - 1;
                    row = NUM_OF_TILES - row - 1;
                }

                double targetCenterX = (col * tileWidth) + (tileWidth / 2.0);
                double targetCenterY = (row * tileHeight) + (tileHeight / 2.0);

                int thX = (int) Math.round(targetCenterX - (moveHighlightWidth / 2.0));
                int thY = (int) Math.round(targetCenterY - (moveHighlightHeight / 2.0));

                g2d.fillOval(thX, thY, (int) Math.round(moveHighlightWidth), (int) Math.round(moveHighlightHeight));
            }
        }

        for (int r = 0; r < NUM_OF_TILES; r++) {
            for (int c = 0; c < NUM_OF_TILES; c++) {
                PieceType pieceType = state.board[r][c];

                if (pieceType != null) {
                    int col = c;
                    int row = r;

                    if (boardFlipped) {
                        col = NUM_OF_TILES - col - 1;
                        row = NUM_OF_TILES - row - 1;
                    }

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