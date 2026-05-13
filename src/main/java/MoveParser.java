public class MoveParser {

    private char piece;
    private int targetCol; // x
    private int targetRow; // y
    private boolean take;

    public MoveParser(String moveString) {
        // 1. Clean the string (remove +, #, or ?)
        String cleanMove = moveString.replaceAll("[+#?!]", "");

        if (cleanMove.contains("x")){
            take = true;
        } else {
            take = false;
        }

        if (Character.isUpperCase(cleanMove.charAt(0))) {
            // It's a Piece (N, B, R, Q, K)
            this.piece = cleanMove.charAt(0);
            String coords = cleanMove.substring(cleanMove.length() - 2);
            parseCoordinates(coords);
        } else {
            // It's a Pawn move (e4, exd5)
            this.piece = 'P';
            String coords = cleanMove.substring(cleanMove.length() - 2);
            parseCoordinates(coords);
        }
    }

    private void parseCoordinates(String coords) {
        // Convert 'a'-'h' to 0-7
        this.targetCol = coords.charAt(0) - 'a';

        // Convert '1'-'8' to 0-7 and flip it
        int rank = Character.getNumericValue(coords.charAt(1));
        this.targetRow = 8 - rank;
    }

    public int getX() {
        return targetCol;
    }

    public int getY() {
        return targetRow;
    }

    public char getPiece() {
        return piece;
    }
}
