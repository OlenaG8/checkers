package Logic;

public enum PieceType {
    VANILLA(true, false),
    CHOCOLATE(false, false),
    VANILLA_QUEEN(true, true),
    CHOCOLATE_QUEEN(false, true);

    private final boolean vanilla;
    private final boolean queen;

    PieceType(boolean vanilla, boolean queen) {
        this.vanilla = vanilla;
        this.queen = queen;
    }

    public boolean isVanilla() {
        return vanilla;
    }

    public boolean isQueen() {
        return queen;
    }
}
