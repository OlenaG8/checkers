package Logic;

import Communication.Messages.Position;

import java.util.ArrayList;
import java.util.List;

import static Logic.GameState.NUM_OF_TILES;

public class MovementLogic {

    public static int canMove(GameState state, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromCol < 0 || toRow < 0 || toCol < 0) return 0;
        int maxPossibleIndex = NUM_OF_TILES - 1;
        if (fromRow > maxPossibleIndex || fromCol > maxPossibleIndex || toRow > maxPossibleIndex || toCol > maxPossibleIndex)
            return 0;

        PieceType checkerValue = state.board[fromRow][fromCol];
        if (checkerValue == null) return 0;
        if (state.board[toRow][toCol] != null) return 0;

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff != colDiff) return 0;

        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);

        if (checkerValue.isQueen()) {
            int piecesInBetween = 0;
            int enemyRow = -1;
            int enemyCol = -1;

            int r = fromRow + rowDir;
            int c = fromCol + colDir;

            while (r != toRow && c != toCol) {
                if (state.board[r][c] != null) {
                    piecesInBetween++;
                    enemyRow = r;
                    enemyCol = c;
                }
                r += rowDir;
                c += colDir;
            }

            if (piecesInBetween == 0) return 1;
            if (piecesInBetween == 1) {
                if (isEnemyOnTile(state, enemyRow, enemyCol, checkerValue)) {
                    return 2;
                }
            }

            return 0;
        } else {
            boolean movesUp = checkerValue.isVanilla();
            boolean movesDown = !checkerValue.isVanilla();

            if (rowDiff == 1) {
                if (movesUp && rowDir == -1) return 1;
                if (movesDown && rowDir == 1) return 1;
            } else if (rowDiff == 2) {
                if (isEnemyOnTile(state, fromRow + rowDir, fromCol + colDir, checkerValue)) {
                    return 2;
                }
            }

            return 0;
        }
    }

    public static boolean hasAnyJumps(GameState state, int row, int col) {
        PieceType checkerValue = state.board[row][col];
        if (checkerValue == null) return false;

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        if (checkerValue.isQueen()) {
            for (int[] d : directions) {
                int r = row + d[0];
                int c = col + d[1];

                while (r >= 0 && r < NUM_OF_TILES && c >= 0 && c < NUM_OF_TILES) {
                    if (state.board[r][c] != null) {
                        if (isEnemyOnTile(state, r, c, checkerValue)) {
                            int landR = r + d[0];
                            int landC = c + d[1];
                            if (landR >= 0 && landR < NUM_OF_TILES && landC >= 0 && landC < NUM_OF_TILES) {
                                if (state.board[landR][landC] == null) {
                                    return true;
                                }
                            }
                        }
                        break;
                    }
                    r += d[0];
                    c += d[1];
                }
            }
        } else {
            for (int[] d : directions) {
                if (canMove(state, row, col, row + d[0] * 2, col + d[1] * 2) == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEnemyOnTile(GameState state, int row, int col, PieceType myPieceValue) {
        if (row < 0 || col < 0 || row > NUM_OF_TILES - 1 || col > NUM_OF_TILES - 1)
            return false;

        PieceType targetValue = state.board[row][col];
        if (targetValue == null) return false;

        return myPieceValue.isVanilla() != targetValue.isVanilla();
    }

    public static boolean hasAnyValidMoves(GameState state, PlayerColor player) {
        for (int r = 0; r < NUM_OF_TILES; r++) {
            for (int c = 0; c < NUM_OF_TILES; c++) {
                PieceType piece = state.board[r][c];
                if (piece == null) continue;

                boolean isCurrentPlayer = piece.matches(player);
                if (isCurrentPlayer) {
                    for (int tr = 0; tr < NUM_OF_TILES; tr++) {
                        for (int tc = 0; tc < NUM_OF_TILES; tc++) {
                            if (canMove(state, r, c, tr, tc) > 0) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static List<Position> getAllowedMoves(GameState state, int row, int col) {
        List<Position> moves = new ArrayList<>();
        for (int r = 0; r < NUM_OF_TILES; r++) {
            for (int c = 0; c < NUM_OF_TILES; c++) {
                int moveType = canMove(state, row, col, r, c);
                if (moveType > 0) {
                    if (state.isJumpingSequence && moveType != 2) continue;
                    moves.add(new Position(r, c));
                }
            }
        }
        return moves;
    }
}