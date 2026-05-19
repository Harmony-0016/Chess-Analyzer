import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.window.rememberWindowState
import java.lang.Math.abs
import java.lang.Integer.signum



@Composable
@Preview
fun app(
    boardState: Array<Array<String>>, // Added this
    onUploadPgnClick: () -> Unit,
    nextMove: () -> Unit,
    previousMove: () -> Unit,
){

    Box(
        //Make the bos take up the whole ui
        modifier = Modifier.fillMaxSize()
        .background(Color(0xFF2C2C2C)) // Dark charcoal background
        .padding(12.dp), // This creates a "frame" around the squares
        contentAlignment = Alignment.Center // This tells the Box to center its children

    ) {
        // The Column now only takes up the space it needs for the board
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ChessBoard(boardState)
                    // Using a Row instead of a Box to put them side-by-side
                    Row(
                        modifier = Modifier
                            .width(300.dp) // Fixed width for the control bar
                            .height(50.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly, // Spaces them out nicely
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = previousMove,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF769656),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = nextMove,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF769656),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next")
                        }
                    }
                }
                Button(
                    onClick = onUploadPgnClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF769656),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                ){
                    Text("Upload PGN")
                }

            }
    }

}

@Composable
fun ChessBoard(boardState: Array<Array<String>>) {
    // The Box stack allows us to put pieces ON TOP of the background
    Box(modifier = Modifier.size((64 * 8).dp)) {
        //This Composable is "Stateless", so Compose skips it during recomposition
        BoardBackground()

        // This Composable only reacts when boardState changes
        PieceOverlay(boardState)
    }
}

@Composable
fun BoardBackground() {
    // This only runs once unless the app theme changes
    Column {
        for (row in 0 until 8) {
            Row {
                for (col in 0 until 8) {
                    val isDark = (row + col) % 2 != 0
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(if (isDark) Color(0xFF769656) else Color(0xFFEBECD0))
                    )
                }
            }
        }
    }
}

@Composable
fun PieceOverlay(boardState: Array<Array<String>>) {
    Column {
        for (row in 0 until 8) {
            Row {
                for (col in 0 until 8) {
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val piece = boardState[row][col]
                        if (piece.isNotEmpty()) {
                            Text(
                                text = getPieceSymbol(piece),
                                fontSize = 40.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getPieceSymbol(piece: String): String {
    return when (piece) {
        "K" -> "♔"
        "Q" -> "♕"
        "R" -> "♖"
        "B" -> "♗"
        "N" -> "♘"
        "P" -> "♙"
        "k" -> "♚"
        "q" -> "♛"
        "r" -> "♜"
        "b" -> "♝"
        "n" -> "♞"
        "p" -> "♟"
        else -> ""
    }
}


fun canReach(board: Array<Array<String>>, piece: String, startR: Int, startC: Int, targetR: Int, targetC: Int): Boolean {
    //Change in x,y and piece type
    val dx = abs(targetC - startC)
    val dy = abs(targetR - startR)
    val p = piece.uppercase()

    //--Knight-- L shape
    if (p == "N") {
        return (dx == 2 && dy == 1) || (dx == 1 && dy == 2)
    }

    //--King-- One square
    if (p == "K") {
        return dx <= 1 && dy <= 1
    }

    //--Rook, Bishop, Queen--
    if (p == "B" || p == "R" || p == "Q") {
        val isDiagonal = dx == dy
        val isStraight = dx == 0 || dy == 0

        //Piece cant move if Bishop isnt diagonal, Rook isnt straight, queen isnt either of the attributes
        if (p == "B" && !isDiagonal) return false
        if (p == "R" && !isStraight) return false
        if (p == "Q" && !isDiagonal && !isStraight) return false

        val stepX = signum(targetC - startC)
        val stepY = signum(targetR - startR)

        var currR = startR + stepY
        var currC = startC + stepX

        //Step through the squares between start and target
        while (currR != targetR || currC != targetC) {
            if (board[currR][currC].isNotEmpty()) {
                return false //path is blocked by a piece
            }
            currR += stepY
            currC += stepX
        }
        return true
    }
    return false
}

@Composable
fun PgnWindowContent(onSave: (String) -> Unit) {
    var pgnInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Paste your PGN data below:", color = Color.Black)
        OutlinedTextField(
            value = pgnInput,
            onValueChange = {pgnInput = it},
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Makes the textbox expand to fill vertical space
            label = { Text("PGN Text") },
            placeholder = { Text("1. e4 e5 2. Nf3 ...") }, //Phantom Text
            singleLine = false // Allows multiple lines for long games
        )
        Button(
            onClick = {
                onSave(pgnInput)
            },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF769656),
                contentColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ){
            Text("Save and Close")
        }

    }
}

fun main() = application {
    //Track to see if PGN is open
    var isPgnWindowOpen by remember { mutableStateOf(false) }

    //Make the Initial Board
    var startingBoard by remember {
        mutableStateOf(
            arrayOf(
                arrayOf("r", "n", "b", "q", "k", "b", "n", "r"),
                arrayOf("p", "p", "p", "p", "p", "p", "p", "p"),
                arrayOf("", "", "", "", "", "", "", ""),
                arrayOf("", "", "", "", "", "", "", ""),
                arrayOf("", "", "", "", "", "", "", ""),
                arrayOf("", "", "", "", "", "", "", ""),
                arrayOf("P", "P", "P", "P", "P", "P", "P", "P"),
                arrayOf("R", "N", "B", "Q", "K", "B", "N", "R")
            )
        )
    }

    var boardHistory by remember { mutableStateOf(listOf(startingBoard)) }
    var currentMoveIndex by remember { mutableStateOf(0) }
    var boardState by remember {mutableStateOf(startingBoard)}
    //Main Window
    Window(
        onCloseRequest = ::exitApplication,
        title = "Chess Analyzer",
        state = rememberWindowState(width = 1000.dp, height = 800.dp)
    ) {
        app(boardState = boardState,
            onUploadPgnClick = { isPgnWindowOpen = true },
            nextMove = {
                if (currentMoveIndex < boardHistory.size - 1) {
                    currentMoveIndex++
                    boardState = boardHistory[currentMoveIndex]
                }
            },
            previousMove = {
                if (currentMoveIndex > 0) {
                    currentMoveIndex--
                    boardState = boardHistory[currentMoveIndex]
                }
            })
    }

    // Helper function to update the board based on a PGN string
    fun applyMove(currentBoard: Array<Array<String>>, moveStr: String, isWhite: Boolean): Array<Array<String>> {
        val nextBoard = currentBoard.map { it.copyOf() }.toTypedArray()
        val parser = MoveParser(moveStr)

        val targetX = parser.getX()
        val targetY = parser.getY()
        val initialX = parser.getCurrentCol() // Assuming MoveParser grabs the file for captures (e.g., 'e' from exd5)
        val initialY = parser.getCurrentRow()

        val pieceType = if (isWhite) {
            parser.piece.uppercaseChar().toString()
        } else {
            parser.piece.lowercaseChar().toString()
        }

        //--Castling Logic--
        if (parser.isKingsideCastle) {
            if (isWhite) {
                nextBoard[7][4] = ""
                nextBoard[7][6] = "K"
                nextBoard[7][7] = ""
                nextBoard[7][5] = "R"
            } else {
                nextBoard[0][4] = ""
                nextBoard[0][6] = "k"
                nextBoard[0][7] = ""
                nextBoard[0][5] = "r"
            }
            return nextBoard
        } else if (parser.isQueensideCastle) {
            if (isWhite) {
                nextBoard[7][4] = ""
                nextBoard[7][2] = "K"
                nextBoard[7][0] = ""
                nextBoard[7][3] = "R"
            } else {
                nextBoard[0][4] = ""
                nextBoard[0][2] = "k"
                nextBoard[0][0] = ""
                nextBoard[0][3] = "r"
            }
            return nextBoard
        }

        //--Pawn Logic--
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

                        //--En Passant--
                        if (isCapture && currentBoard[targetY][targetX] == "") {
                            nextBoard[r][targetX] = ""
                        }

                        //--Promotion--
                        if (parser.promotion != ' ') {
                            val promotedPiece = if (isWhite) {
                                parser.promotion.uppercaseChar().toString()
                            } else {
                                parser.promotion.lowercaseChar().toString()
                            }
                            nextBoard[targetY][targetX] = promotedPiece
                        } else {
                            // Normal pawn move
                            nextBoard[targetY][targetX] = pieceType
                        }
                        return nextBoard
                    }
                }
            }
        }
        //--Piece Logic--
        else {
            for (r in 0..7) {
                for (c in 0..7) {
                    val pieceMatches = currentBoard[r][c] == pieceType
                    val colMatches = (initialX == -1 || initialX == c)
                    val rowMatches = (initialY == -1 || initialY == r)

                    //Can the current piece see the position
                    val canPhysicallyReach = canReach(currentBoard, pieceType, r, c, targetY, targetX)

                    if (pieceMatches && colMatches && rowMatches && canPhysicallyReach) {
                        nextBoard[r][c] = ""
                        nextBoard[targetY][targetX] = pieceType
                        return nextBoard
                    }
                }
            }
        }
        return nextBoard
    }

    //Shows if PGN window is open
    if (isPgnWindowOpen) {
        Window(
            onCloseRequest = { isPgnWindowOpen = false }, // Close this window only
            title = "PGN Uploader",
            state = rememberWindowState(width = 400.dp, height = 300.dp)
        ) {
            //TODO: Add move tracking and display
            PgnWindowContent(onSave = { rawText ->
                val parser = DataParser(rawText)
                val allMoves = parser.moves

                val newHistory = mutableListOf(startingBoard)
                var tempBoard = startingBoard

                // Loop through the move pairs
                for (movePair in allMoves) {
                    // Apply White's move
                    if (movePair[0].isNotEmpty()) {
                        tempBoard = applyMove(tempBoard, movePair[0], true)
                        newHistory.add(tempBoard)
                    }
                    // Apply Black's move
                    if (movePair[1].isNotEmpty()) {
                        tempBoard = applyMove(tempBoard, movePair[1], false)
                        newHistory.add(tempBoard)
                    }
                }

                // Update the state
                boardHistory = newHistory
                currentMoveIndex = 0
                boardState = boardHistory[0]
                isPgnWindowOpen = false
            })
        }
    }
}