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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.lang.Integer.signum



@Composable
@Preview
fun app(
    boardState: Array<Array<String>>,
    player1: String,
    player2: String,
    pgnState: String,
    recomendedMoves: String,
    currentEval: Float,
    isMate: Boolean,
    mateIn: Int,
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
                EvaluationBar(eval = currentEval, isMate = isMate, mateIn = mateIn)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = player2,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ChessBoard(boardState)

                    Text(
                        text = player1,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

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

                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    Text("Content: $pgnState",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

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
                    //Box with the stockfish recommendations
                    Box (
                        modifier = Modifier
                            .width(300.dp)
                            .height(345.dp)
                            .background(color = Color(0xFF769656), shape = RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            text = recomendedMoves,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

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

@Composable
fun EvaluationBar(eval: Float, isMate: Boolean, mateIn: Int) {
    val clampLimit = 5f

    val whitePercentage = when {
        isMate && mateIn > 0 -> 1f
        isMate && mateIn < 0 -> 0f
        else -> {
            val clampedEval = eval.coerceIn(-clampLimit, clampLimit)
            (clampedEval + clampLimit) / (clampLimit * 2)
        }
    }

    val evalText = when {
        isMate -> "M${kotlin.math.abs(mateIn)}"
        eval > 0 -> "+${String.format("%.1f", eval)}"
        else -> String.format("%.1f", eval)
    }

    Box(
        modifier = Modifier
            .width(24.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f - whitePercentage + 0.001f)
                    .background(Color(0xFF404040)),
                contentAlignment = Alignment.TopCenter
            ) {
                if (eval < 0 || (isMate && mateIn < 0)) {
                    Text(
                        text = evalText,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(whitePercentage + 0.001f)
                    .background(Color.White),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (eval >= 0 || (isMate && mateIn > 0)) {
                    Text(
                        text = evalText,
                        color = Color.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

fun main() = application {
    //Track to see if PGN is open
    var isPgnWindowOpen by remember { mutableStateOf(false) }
    var player1 by remember { mutableStateOf("White Player: ?") }
    var player2 by remember { mutableStateOf("Black Player: ?") }
    var pgnState by remember { mutableStateOf("Empty") }

    // Add these next to your other state variables
    var currentEval by remember { mutableStateOf(0f) }
    var isMate by remember { mutableStateOf(false) }
    var mateIn by remember { mutableStateOf(0) }

    //TODO: Make a button to let the person change the number of next best moves
    var numberOfDesiredMoves = 3

    var moveList by remember { mutableStateOf(emptyList<String>())}

    //TODO: Make the interface with stockfish but do not interrupt processes with it.
    val stockfish = remember {
        val engine  = StockFishEngine("stockfish_x86-64-avx512.exe")
        engine.startEngine()
        engine
    }

    //Coroutine for background tasks
    val coroutineScope = rememberCoroutineScope()
    var engineOutputText by remember {mutableStateOf("To Start, press upload PGN. Navigate to your chess game. \n " +
            "Press share and select the PGN option. The UI will update to the information given.\n" +
            "Content will show empty until data has been process.")}

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
        onCloseRequest = {
            stockfish.stopEngine()
            exitApplication()
        },
        title = "Chess Analyzer",
        state = rememberWindowState(width = 1000.dp, height = 800.dp)
    ) {
        app(boardState = boardState,
            player1 = player1,
            player2 = player2,
            pgnState = pgnState,
            recomendedMoves = engineOutputText,
            currentEval = currentEval,
            isMate = isMate,
            mateIn = mateIn,
            onUploadPgnClick = { isPgnWindowOpen = true },
            nextMove = {
                if (currentMoveIndex < boardHistory.size - 1) {
                    currentMoveIndex++
                    boardState = boardHistory[currentMoveIndex]

                    val currentStockfishMoves = moveList.take(currentMoveIndex).joinToString(" ")

                    engineOutputText = "Calculating..."

                    coroutineScope.launch(Dispatchers.IO) {
                        val topMoves = stockfish.getTopMoves(currentStockfishMoves, 2000, numberOfDesiredMoves)

                        if (topMoves.isNotEmpty()) {
                            val bestMove = topMoves[0]
                            isMate = bestMove.isMate
                            mateIn = bestMove.mateIn
                            currentEval = bestMove.centipawns / 100f
                        }

                        var resultText = ""
                        for((index, engineMove) in topMoves.withIndex()) {
                            resultText += "Rank ${index + 1}: ${engineMove.move} \n"
                        }

                        engineOutputText = resultText
                    }

                }
            },
            previousMove = {
                if (currentMoveIndex > 0) {
                    currentMoveIndex--
                    boardState = boardHistory[currentMoveIndex]

                    val currentStockfishMoves = moveList.take(currentMoveIndex).joinToString(" ")

                    engineOutputText = "Calculating..."

                    coroutineScope.launch(Dispatchers.IO) {
                        val topMoves = stockfish.getTopMoves(currentStockfishMoves, 2000, numberOfDesiredMoves)

                        if (topMoves.isNotEmpty()) {
                            val bestMove = topMoves[0]
                            isMate = bestMove.isMate
                            mateIn = bestMove.mateIn
                            currentEval = bestMove.centipawns / 100f
                        }

                        var resultText = ""
                        for ((index, engineMove) in topMoves.withIndex()) {
                            resultText += "Rank ${index + 1}: ${engineMove.move} \n"
                        }

                        engineOutputText = resultText
                    }

                }
            })
    }

    //Helper function to change the PGN notation to UCI
    fun toUciSquare(row: Int, col: Int): String {
        val file = ('a' + col).toString()
        val rank = (8 - row).toString()
        return file + rank
    }

    // Helper function to update the board based on a PGN string
    fun applyMove(currentBoard: Array<Array<String>>, moveStr: String, isWhite: Boolean): Pair<Array<Array<String>>, String> {
        val nextBoard = currentBoard.map { it.copyOf() }.toTypedArray()
        val parser = MoveParser(moveStr)

        val targetX = parser.x
        val targetY = parser.y
        val initialX = parser.currentCol
        val initialY = parser.currentRow

        val pieceType = if (isWhite) {
            parser.piece.uppercaseChar().toString()
        } else {
            parser.piece.lowercaseChar().toString()
        }

        //--Castling Logic--
        if (parser.isKingsideCastle) {
            val uci = if(isWhite) "e1g1" else "e8g8"
            if (isWhite) {
                nextBoard[7][4] = ""; nextBoard[7][6] = "K"; nextBoard[7][7] = ""; nextBoard[7][5] = "R"
            } else {
                nextBoard[0][4] = ""; nextBoard[0][6] = "k"; nextBoard[0][7] = ""; nextBoard[0][5] = "r"
            }
            return Pair(nextBoard, uci)
        } else if (parser.isQueensideCastle) {
            val uci = if (isWhite) "e1c1" else "e8c8"
            if (isWhite) {
                nextBoard[7][4] = ""; nextBoard[7][2] = "K"; nextBoard[7][0] = ""; nextBoard[7][3] = "R"
            } else {
                nextBoard[0][4] = ""; nextBoard[0][2] = "k"; nextBoard[0][0] = ""; nextBoard[0][3] = "r"
            }
            return Pair(nextBoard, uci)
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

                        // Generate base UCI string (e.g., "e2e4")
                        val startSquare = toUciSquare(r, searchCol)
                        val endSquare = toUciSquare(targetY, targetX)
                        var uciMove = startSquare + endSquare

                        //--En Passant--
                        if (isCapture && currentBoard[targetY][targetX] == "") {
                            nextBoard[r][targetX] = ""
                        }

                        //--Promotion--
                        // Note: ensure your parser variable matches. It may be 'promotedTo' instead of 'promotion'
                        if (parser.promotion != ' ') {
                            val promotedPiece = if (isWhite) {
                                parser.promotion.uppercaseChar().toString()
                            } else {
                                parser.promotion.lowercaseChar().toString()
                            }
                            nextBoard[targetY][targetX] = promotedPiece
                            // UCI requires the lowercase letter for promotions (e.g., "e7e8q")
                            uciMove += parser.promotion.lowercaseChar()
                        } else {
                            // Normal pawn move
                            nextBoard[targetY][targetX] = pieceType
                        }

                        return Pair(nextBoard, uciMove)
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

                    // Can the current piece see the position
                    val canPhysicallyReach = canReach(currentBoard, pieceType, r, c, targetY, targetX)

                    if (pieceMatches && colMatches && rowMatches && canPhysicallyReach) {
                        nextBoard[r][c] = ""
                        nextBoard[targetY][targetX] = pieceType

                        // Generate base UCI string for pieces
                        val startSquare = toUciSquare(r, c)
                        val endSquare = toUciSquare(targetY, targetX)
                        val uciMove = startSquare + endSquare

                        return Pair(nextBoard, uciMove)
                    }
                }
            }
        }

        // Fallback if no valid move was found (prevents compiler errors)
        return Pair(nextBoard, "")
    }

    //Shows if PGN window is open
    if (isPgnWindowOpen) {
        Window(
            onCloseRequest = { isPgnWindowOpen = false }, // Close this window only
            title = "PGN Uploader",
            state = rememberWindowState(width = 400.dp, height = 300.dp)
        ) {
            PgnWindowContent(onSave = { rawText ->
                val parser = DataParser(rawText)
                val allMoves = parser.moves

                val newHistory = mutableListOf(startingBoard)
                val uciMoves = mutableListOf<String>()
                var tempBoard = startingBoard

                for (movePair in allMoves) {
                    // Apply White's move
                    if (movePair[0].isNotEmpty()) {
                        val result = applyMove(tempBoard, movePair[0], true)
                        tempBoard = result.first      // Grab the board
                        uciMoves.add(result.second)   // Grab the "e2e4" string
                        newHistory.add(tempBoard)

                    }

                    // Apply Black's move
                    if (movePair.size > 1 && movePair[1].isNotEmpty()) {
                        val result = applyMove(tempBoard, movePair[1], false)
                        tempBoard = result.first
                        uciMoves.add(result.second)
                        newHistory.add(tempBoard)
                    }
                }

                //Save to your Compose state
                moveList = uciMoves
                player1 = "${parser.white} (${parser.whiteElo})"
                player2 = "${parser.black} (${parser.blackElo})"
                pgnState = "Filled"

                boardHistory = newHistory
                currentMoveIndex = 0
                boardState = boardHistory[0]
                isPgnWindowOpen = false
            })
        }
    }
}