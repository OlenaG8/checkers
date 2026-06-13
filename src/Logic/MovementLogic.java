package Logic;

public class MovementLogic {
    public static final int NUM_OF_TILES = 8;
    public static final int ROWS_PER_COLOR = 3;

    public static final PieceType[][] boardState = new PieceType[NUM_OF_TILES][NUM_OF_TILES];
    private static CheckersStartPosition whitePosition = MovementLogic.CheckersStartPosition.WHITE_ON_BOTTOM;

    public static void Initialize(CheckersStartPosition whitePosition) {
        for (int row = 0; row < NUM_OF_TILES; row++)
            for (int col = 0; col < NUM_OF_TILES; col++)
                boardState[row][col] = null;

        MovementLogic.whitePosition = whitePosition;
        int startBottomPos = 5;
        int endBottomPos = startBottomPos + ROWS_PER_COLOR;

        PieceType curColor = whitePosition == CheckersStartPosition.WHITE_ON_BOTTOM ? PieceType.VANILLA : PieceType.CHOCOLATE;
        for (int row = startBottomPos; row < endBottomPos; ++row)
            for (int col = row % 2; col < NUM_OF_TILES; col += 2)
                boardState[row][col] = curColor;

        int startTopPos = 0;

        curColor = whitePosition == MovementLogic.CheckersStartPosition.WHITE_ON_TOP ?  PieceType.VANILLA : PieceType.CHOCOLATE;
        for (int row = startTopPos; row < ROWS_PER_COLOR; ++row)
            for (int col = row % 2; col < NUM_OF_TILES; col += 2)
                boardState[row][col] = curColor;

    }

    public static int MoveCheckerOnce(int fromRow, int fromCol, int toRow, int toCol) {
        int canCheckerMoveResult = CanCheckerMoveOnce(fromRow, fromCol, toRow, toCol);
        if (canCheckerMoveResult == 0)
            return 0;

        PieceType checkerToMove = boardState[fromRow][fromCol];
        boardState[fromRow][fromCol] = null;
        boardState[toRow][toCol] = checkerToMove;

        if (canCheckerMoveResult == 2) {
            int rowDir = Integer.compare(toRow, fromRow);
            int colDir = Integer.compare(toCol, fromCol);

            int r = fromRow + rowDir;
            int c = fromCol + colDir;

            while (r != toRow && c != toCol) {
                boardState[r][c] = null;
                r += rowDir;
                c += colDir;
            }
        }

        if (checkerToMove == PieceType.VANILLA) {
            int promotionRow = (whitePosition == CheckersStartPosition.WHITE_ON_BOTTOM) ? 0 : NUM_OF_TILES - 1;
            if (toRow == promotionRow) boardState[toRow][toCol] = PieceType.VANILLA_QUEEN;
        } else if (checkerToMove == PieceType.CHOCOLATE) {
            int promotionRow = (whitePosition == CheckersStartPosition.WHITE_ON_BOTTOM) ? NUM_OF_TILES - 1 : 0;
            if (toRow == promotionRow) boardState[toRow][toCol] = PieceType.CHOCOLATE_QUEEN;
        }

        return canCheckerMoveResult;
    }

    public static int CanCheckerMoveOnce(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromCol < 0 || toRow < 0 || toCol < 0) return 0;
        int maxPossibleIndex = NUM_OF_TILES - 1;
        if (fromRow > maxPossibleIndex || fromCol > maxPossibleIndex || toRow > maxPossibleIndex || toCol > maxPossibleIndex)
            return 0;

        PieceType checkerValue = boardState[fromRow][fromCol];
        if (checkerValue == null) return 0;
        if (boardState[toRow][toCol] != null) return 0;

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
                if (boardState[r][c] != null) {
                    piecesInBetween++;
                    enemyRow = r;
                    enemyCol = c;
                }
                r += rowDir;
                c += colDir;
            }

            if (piecesInBetween == 0) return 1;
            if (piecesInBetween == 1) {
                if (isEnemyOnTile(enemyRow, enemyCol, checkerValue)) {
                    return 2;
                }
            }

            return 0;
        } else {
            boolean movesUp = ((whitePosition == CheckersStartPosition.WHITE_ON_BOTTOM) == checkerValue.isVanilla());
            boolean movesDown = ((whitePosition == CheckersStartPosition.WHITE_ON_BOTTOM) != checkerValue.isVanilla());

            if (rowDiff == 1) {
                if (movesUp && rowDir == -1) return 1;
                if (movesDown && rowDir == 1) return 1;
            } else if (rowDiff == 2) {
                if (isEnemyOnTile(fromRow + rowDir, fromCol + colDir, checkerValue)) {
                    return 2;
                }
            }

            return 0;
        }
    }

    public static boolean HasAnyJumps(int row, int col) {
        PieceType checkerValue = boardState[row][col];
        if (checkerValue == null) return false;

        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        if (checkerValue.isQueen()) {
            for (int[] d : directions) {
                int r = row + d[0];
                int c = col + d[1];

                while (r >= 0 && r < NUM_OF_TILES && c >= 0 && c < NUM_OF_TILES) {
                    if (boardState[r][c] != null) {
                        if (isEnemyOnTile(r, c, checkerValue)) {
                            int landR = r + d[0];
                            int landC = c + d[1];
                            if (landR >= 0 && landR < NUM_OF_TILES && landC >= 0 && landC < NUM_OF_TILES) {
                                if (boardState[landR][landC] == null) {
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
                if (CanCheckerMoveOnce(row, col, row + d[0] * 2, col + d[1] * 2) == 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEnemyOnTile(int row, int col, PieceType myPieceValue) {
        if (row < 0 || col < 0 || row > NUM_OF_TILES - 1 || col > NUM_OF_TILES - 1)
            return false;

        PieceType targetValue = boardState[row][col];
        if (targetValue == null) return false;

        return myPieceValue.isVanilla() != targetValue.isVanilla();
    }

    public enum CheckersStartPosition {
        WHITE_ON_TOP, WHITE_ON_BOTTOM
    }

    public static boolean HasAnyValidMoves(int player) {
        for (int r = 0; r < NUM_OF_TILES; r++) {
            for (int c = 0; c < NUM_OF_TILES; c++) {
                PieceType piece = boardState[r][c];
                if (piece == null) continue;

                boolean isCurrentPlayer = (player == 1) == piece.isVanilla();

                if (isCurrentPlayer) {
                    for (int tr = 0; tr < NUM_OF_TILES; tr++) {
                        for (int tc = 0; tc < NUM_OF_TILES; tc++) {
                            if (CanCheckerMoveOnce(r, c, tr, tc) > 0) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}