package Logic;

public class GameState {
    public static final int NUM_OF_TILES = 8;
    public static final int ROWS_PER_COLOR = 3;

    public final PieceType[][] board = new PieceType[NUM_OF_TILES][NUM_OF_TILES];
    public final StartPosition whitePosition;

    public boolean isJumpingSequence;

    public GameState(StartPosition whitePosition) {
        this.whitePosition = whitePosition;
        int startBottomPos = 5;
        int endBottomPos = startBottomPos + ROWS_PER_COLOR;

        PieceType curColor = whitePosition == StartPosition.VANILLA_ON_BOTTOM ? PieceType.VANILLA : PieceType.CHOCOLATE;
        for (int row = startBottomPos; row < endBottomPos; ++row)
            for (int col = row % 2; col < NUM_OF_TILES; col += 2)
                board[row][col] = curColor;

        int startTopPos = 0;

        curColor = whitePosition == StartPosition.VANILLA_ON_TOP ? PieceType.VANILLA : PieceType.CHOCOLATE;
        for (int row = startTopPos; row < ROWS_PER_COLOR; ++row)
            for (int col = row % 2; col < NUM_OF_TILES; col += 2)
                board[row][col] = curColor;
    }

    public MoveResult move(int fromRow, int fromCol, int toRow, int toCol) {
        int res = MovementLogic.canMove(this, fromRow, fromCol, toRow, toCol);
        if (res == 0) return MoveResult.INVALID_MOVE;

        PieceType checkerToMove = board[fromRow][fromCol];
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = checkerToMove;

        if (res == 2) {
            int rowDir = Integer.compare(toRow, fromRow);
            int colDir = Integer.compare(toCol, fromCol);

            int r = fromRow + rowDir;
            int c = fromCol + colDir;

            while (r != toRow && c != toCol) {
                board[r][c] = null;
                r += rowDir;
                c += colDir;
            }
        }

        if (checkerToMove == PieceType.VANILLA) {
            int promotionRow = (whitePosition == StartPosition.VANILLA_ON_BOTTOM) ? 0 : NUM_OF_TILES - 1;
            if (toRow == promotionRow) board[toRow][toCol] = PieceType.VANILLA_QUEEN;
        } else if (checkerToMove == PieceType.CHOCOLATE) {
            int promotionRow = (whitePosition == StartPosition.VANILLA_ON_BOTTOM) ? NUM_OF_TILES - 1 : 0;
            if (toRow == promotionRow) board[toRow][toCol] = PieceType.CHOCOLATE_QUEEN;
        }
        if (res == 2 && MovementLogic.hasAnyJumps(this, toRow, toCol)) {
            this.isJumpingSequence = true;
            return MoveResult.ENTER_JUMP_SEQUENCE;
        } else {
            return MoveResult.END_TURN;
        }
    }

    public enum StartPosition {
        VANILLA_ON_TOP,
        VANILLA_ON_BOTTOM
    }
}
