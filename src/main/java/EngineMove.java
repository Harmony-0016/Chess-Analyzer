public class EngineMove {
    private String move;
    private int centipawns;
    private boolean isMate;
    private int mateIn;

    public EngineMove(String move, int centipawns, boolean isMate, int mateIn) {
        this.move = move;
        this.centipawns = centipawns;
        this.isMate = isMate;
        this.mateIn = mateIn;
    }

    // Getters
    public String getMove() { return move; }
    public int getCentipawns() { return centipawns; }
    public boolean isMate() { return isMate; }
    public int getMateIn() { return mateIn; }
}
