public class MoveParser {

    /**
     * This class organizes the information given by a move to determine which pieces should move and how
     * @Author: Liam Guillemette
     */

    private final char piece;
    private char promotedTo = ' ';
    private int currentCol = -1; // Default to -1 (unknown)
    private int currentRow = -1; // Default to -1 (unknown)
    private int targetCol;
    private int targetRow;
    private boolean take;
    private boolean isKingsideCastle = false;
    private boolean isQueensideCastle = false;

    public MoveParser(String moveString) {
        //Make the string remove all weird symbols
        String cleanMove = moveString.replaceAll("[+#?!]", "");
        this.take = cleanMove.contains("x");

        //Handles castling
        if (cleanMove.equals("O-O")) {
            this.isKingsideCastle = true;
            this.piece = 'K';
            return;
        } else if (cleanMove.equals("O-O-O")) {
            this.isQueensideCastle = true;
            this.piece = 'K';
            return;
        }

        //Handles Promotions
        if (cleanMove.contains("=")) {
            int equalsIndex = cleanMove.indexOf("=");
            this.promotedTo = cleanMove.charAt(equalsIndex + 1);
            cleanMove = cleanMove.substring(0, equalsIndex);
        }

        //remove the x
        String strippedMove = cleanMove.replace("x", "");

        if (Character.isUpperCase(cleanMove.charAt(0))) {
            //Get the type of piece
            this.piece = cleanMove.charAt(0);

            //Get the final position (always the last two characters)
            String coords = cleanMove.substring(cleanMove.length() - 2);
            parseCoordinates(coords);

            if (strippedMove.length() > 3) {
                // Grab whatever is between the Piece and the Destination
                String disambiguation = strippedMove.substring(1, strippedMove.length() - 2);

                if (disambiguation.length() == 1) {
                    //position distinguisher
                    char c = disambiguation.charAt(0);

                    //decide which position it is
                    if (Character.isLetter(c)) {
                        this.currentCol = c - 'a'; // It's a file (e.g., Nbd7)
                    } else if (Character.isDigit(c)) {
                        this.currentRow = 8 - Character.getNumericValue(c); // It's a rank (e.g., N1c3)
                    }

                } else if (disambiguation.length() == 2) {
                    // Full starting coordinate provided (rare, e.g., Qh4xe1)
                    this.currentCol = disambiguation.charAt(0) - 'a';
                    this.currentRow = 8 - Character.getNumericValue(disambiguation.charAt(1));
                }
            }
        } else {
            //it is a pawn
            this.piece = 'P';

            String coords = cleanMove.substring(cleanMove.length() - 2);
            parseCoordinates(coords);

            if (this.take) {
                this.currentCol = cleanMove.charAt(0) - 'a';
            } else {
                this.currentCol = this.targetCol;
            }
        }
    }

    private void parseCoordinates(String coords) {
        // Convert 'a'-'h' to 0-7
        this.targetCol = coords.charAt(0) - 'a';

        // Convert '1'-'8' to 0-7 and flip it
        int rank = Character.getNumericValue(coords.charAt(1));
        this.targetRow = 8 - rank;
    }

    private void parsePosition(String position){
        this.currentCol = position.charAt(0) - 'a';

        int rank = Character.getNumericValue(position.charAt(1));
        this.currentRow = 8 - rank;
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

    public int getCurrentCol(){
        return currentCol;
    }

    public int getCurrentRow(){
        return currentRow;
    }

    public char getPromotion(){
        return promotedTo;
    }

    public boolean getIsKingsideCastle(){
        return isKingsideCastle;
    }

    public boolean getIsQueensideCastle(){
        return isQueensideCastle;
    }
}
