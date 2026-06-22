import java.lang.Integer.signum

/**
 * Gets the piece symbol
 * TODO: Change the symbol to an image for cleanliness
 */
fun getPieceSymbol(piece: String): String {
    return when (piece) {
        "K" -> "♔"; "Q" -> "♕"; "R" -> "♖"; "B" -> "♗"; "N" -> "♘"; "P" -> "♙"
        "k" -> "♚"; "q" -> "♛"; "r" -> "♜"; "b" -> "♝"; "n" -> "♞"; "p" -> "♟"
        else -> ""
    }
}

/**
 * Can a piece touch the desired end position. Is it in vision?
 */
fun canReach(board: Array<Array<String>>, piece: String, startR: Int, startC: Int, targetR: Int, targetC: Int): Boolean {
    //Displacement calculations
    val dx = kotlin.math.abs(targetC - startC)
    val dy = kotlin.math.abs(targetR - startR)
    val p = piece.uppercase()

    //If the piece is a night
    if (p == "N") return (dx == 2 && dy == 1) || (dx == 1 && dy == 2)
    if (p == "K") return dx <= 1 && dy <= 1

    //If it's a bishop, rook or queen
    if (p == "B" || p == "R" || p == "Q") {
        val isDiagonal = dx == dy
        val isStraight = dx == 0 || dy == 0

        if (p == "B" && !isDiagonal) return false
        if (p == "R" && !isStraight) return false
        if (p == "Q" && !isDiagonal && !isStraight) return false

        val stepX = signum(targetC - startC)
        val stepY = signum(targetR - startR)
        var currR = startR + stepY
        var currC = startC + stepX

        while (currR != targetR || currC != targetC) {
            if (board[currR][currC].isNotEmpty()) return false
            currR += stepY
            currC += stepX
        }
        return true
    }
    return false
}

/**
 * Convert square to UCI
 */
fun toUciSquare(row: Int, col: Int): String {
    val file = ('a' + col).toString()
    val rank = (8 - row).toString()
    return file + rank
}

/**
 * Convert UCI into Square
 */
fun formatUciToHuman(uci: String, boardState: Array<Array<String>>): String {
    if (uci.length < 4) return uci

    if (uci == "e1g1" || uci == "e8g8") return "O-O (Kingside)"
    if (uci == "e1c1" || uci == "e8c8") return "O-O-O (Queenside)"


    val startFile = uci[0]
    val startRank = uci[1].toString().toIntOrNull() ?: 8
    val endSquare = uci.substring(2, 4)

    val startCol = startFile - 'a'
    val startRow = 8 - startRank

    if (startRow !in 0..7 || startCol !in 0..7) return uci

    val piece = boardState[startRow][startCol].uppercase()

    return when (piece) {
        "P" -> endSquare
        ""  -> uci
        else -> "$piece$endSquare"
    }
}

/**
 * Creates the next arrays for a given board
 */
fun applyMove(currentBoard: Array<Array<String>>, moveStr: String, isWhite: Boolean): Pair<Array<Array<String>>, String> {
    val nextBoard = currentBoard.map { it.copyOf() }.toTypedArray()
    val parser = MoveParser(moveStr)
    val targetX = parser.x; val targetY = parser.y
    val initialX = parser.currentCol; val initialY = parser.currentRow
    val pieceType = if (isWhite) parser.piece.uppercaseChar().toString() else parser.piece.lowercaseChar().toString()

    if (parser.isKingsideCastle) {
        val uci = if (isWhite) "e1g1" else "e8g8"
        if (isWhite) { nextBoard[7][4] = ""; nextBoard[7][6] = "K"; nextBoard[7][7] = ""; nextBoard[7][5] = "R" }
        else { nextBoard[0][4] = ""; nextBoard[0][6] = "k"; nextBoard[0][7] = ""; nextBoard[0][5] = "r" }
        return Pair(nextBoard, uci)
    } else if (parser.isQueensideCastle) {
        val uci = if (isWhite) "e1c1" else "e8c8"
        if (isWhite) { nextBoard[7][4] = ""; nextBoard[7][2] = "K"; nextBoard[7][0] = ""; nextBoard[7][3] = "R" }
        else { nextBoard[0][4] = ""; nextBoard[0][2] = "k"; nextBoard[0][0] = ""; nextBoard[0][3] = "r" }
        return Pair(nextBoard, uci)
    }

    if (parser.piece.uppercaseChar() == 'P') {
        val searchCol = if (initialX != -1) initialX else targetX
        val direction = if (isWhite) -1 else 1

        for (r in 0..7) {
            if (currentBoard[r][searchCol] == pieceType) {
                val isOneStep = (r + direction == targetY) && (searchCol == targetX)
                val isTwoStep = (r + (2 * direction) == targetY) && (searchCol == targetX) && ((isWhite && r == 6) || (!isWhite && r == 1))
                val isCapture = (r + direction == targetY) && (searchCol != targetX)

                if (isOneStep || isTwoStep || isCapture) {
                    nextBoard[r][searchCol] = ""
                    var uciMove = toUciSquare(r, searchCol) + toUciSquare(targetY, targetX)

                    if (isCapture && currentBoard[targetY][targetX] == "") nextBoard[r][targetX] = ""

                    if (parser.promotion != ' ') {
                        nextBoard[targetY][targetX] = if (isWhite) parser.promotion.uppercaseChar().toString() else parser.promotion.lowercaseChar().toString()
                        uciMove += parser.promotion.lowercaseChar()
                    } else {
                        nextBoard[targetY][targetX] = pieceType
                    }
                    return Pair(nextBoard, uciMove)
                }
            }
        }
    } else {
        for (r in 0..7) {
            for (c in 0..7) {
                val pieceMatches = currentBoard[r][c] == pieceType
                val colMatches = (initialX == -1 || initialX == c)
                val rowMatches = (initialY == -1 || initialY == r)
                if (pieceMatches && colMatches && rowMatches && canReach(currentBoard, pieceType, r, c, targetY, targetX)) {
                    nextBoard[r][c] = ""
                    nextBoard[targetY][targetX] = pieceType
                    return Pair(nextBoard, toUciSquare(r, c) + toUciSquare(targetY, targetX))
                }
            }
        }
    }
    return Pair(nextBoard, "")
}