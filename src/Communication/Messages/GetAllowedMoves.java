package Communication.Messages;

public class GetAllowedMoves {
    private final int row;
    private final int col;

    public GetAllowedMoves(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }


}
