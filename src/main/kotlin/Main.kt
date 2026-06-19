import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.Math.abs
import java.lang.Integer.signum

//THEME COLOURS
val ChessGreen = Color(0xFF769656)
val ChessLight = Color(0xFFEBECD0)
val DarkBackground = Color(0xFF2C2C2C)
val EvalDark = Color(0xFF404040)

//UI
@Composable
@Preview
fun ChessApp(
    boardState: Array<Array<String>>,
    player1: String,
    player2: String,
    pgnState: String,
    recommendedMoves: String,
    currentEval: Float,
    isMate: Boolean,
    mateIn: Int,
    onUploadPgnClick: () -> Unit,
    nextMove: () -> Unit,
    previousMove: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(DarkBackground)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EvaluationBar(eval = currentEval, isMate = isMate, mateIn = mateIn)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = player2, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ChessBoard(boardState)
                Text(text = player1, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.width(300.dp).height(50.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = previousMove,
                        colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Previous") }

                    Button(
                        onClick = nextMove,
                        colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Next") }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Content: $pgnState", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = onUploadPgnClick,
                    colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                ) { Text("Upload PGN") }

                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(345.dp)
                        .background(color = ChessGreen, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Text(text = recommendedMoves, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- SUB-COMPOSABLES ---
@Composable
fun ChessBoard(boardState: Array<Array<String>>) {
    Box(modifier = Modifier.size((64 * 8).dp)) {
        BoardBackground()
        PieceOverlay(boardState)
    }
}

@Composable
fun BoardBackground() {
    Column {
        for (row in 0 until 8) {
            Row {
                for (col in 0 until 8) {
                    val isDark = (row + col) % 2 != 0
                    Box(modifier = Modifier.size(64.dp).background(if (isDark) ChessGreen else ChessLight))
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
                    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                        val piece = boardState[row][col]
                        if (piece.isNotEmpty()) {
                            Text(text = getPieceSymbol(piece), fontSize = 40.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PgnWindowContent(onSave: (String) -> Unit) {
    var pgnInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Paste your PGN data below:", color = Color.Black)
        OutlinedTextField(
            value = pgnInput,
            onValueChange = { pgnInput = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            label = { Text("PGN Text") },
            placeholder = { Text("1. e4 e5 2. Nf3 ...") },
            singleLine = false
        )
        Button(
            onClick = { onSave(pgnInput) },
            colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) { Text("Save and Close") }
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

    Box(modifier = Modifier.width(24.dp).height(400.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, Color.Black, RoundedCornerShape(4.dp))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f - whitePercentage + 0.001f).background(EvalDark),
                contentAlignment = Alignment.TopCenter
            ) {
                if (eval < 0 || (isMate && mateIn < 0)) {
                    Text(text = evalText, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(whitePercentage + 0.001f).background(Color.White),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (eval >= 0 || (isMate && mateIn > 0)) {
                    Text(text = evalText, color = Color.Black, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

// --- HELPER LOGIC FUNCTIONS ---
fun getPieceSymbol(piece: String): String {
    return when (piece) {
        "K" -> "♔"; "Q" -> "♕"; "R" -> "♖"; "B" -> "♗"; "N" -> "♘"; "P" -> "♙"
        "k" -> "♚"; "q" -> "♛"; "r" -> "♜"; "b" -> "♝"; "n" -> "♞"; "p" -> "♟"
        else -> ""
    }
}

fun canReach(board: Array<Array<String>>, piece: String, startR: Int, startC: Int, targetR: Int, targetC: Int): Boolean {
    val dx = kotlin.math.abs(targetC - startC)
    val dy = kotlin.math.abs(targetR - startR)
    val p = piece.uppercase()

    if (p == "N") return (dx == 2 && dy == 1) || (dx == 1 && dy == 2)
    if (p == "K") return dx <= 1 && dy <= 1

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

fun toUciSquare(row: Int, col: Int): String {
    val file = ('a' + col).toString()
    val rank = (8 - row).toString()
    return file + rank
}

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

// --- MAIN APPLICATION ---
fun main() = application {
    // 1. App State
    var isPgnWindowOpen by remember { mutableStateOf(false) }
    var player1 by remember { mutableStateOf("White Player: ?") }
    var player2 by remember { mutableStateOf("Black Player: ?") }
    var pgnState by remember { mutableStateOf("Empty") }

    // 2. Engine State
    var currentEval by remember { mutableStateOf(0f) }
    var isMate by remember { mutableStateOf(false) }
    var mateIn by remember { mutableStateOf(0) }
    var engineOutputText by remember { mutableStateOf("Upload a PGN to begin.") }
    val numberOfDesiredMoves = 3
    var moveList by remember { mutableStateOf(emptyList<String>()) }

    // 3. Thread Management
    val coroutineScope = rememberCoroutineScope()
    var calculationJob by remember { mutableStateOf<Job?>(null) } // Safety toggle to prevent lag!

    val stockfish = remember {
        val engine = StockFishEngine("stockfish_x86-64-avx512.exe")
        engine.startEngine()
        engine
    }

    // 4. Board State
    val startingBoard = arrayOf(
        arrayOf("r", "n", "b", "q", "k", "b", "n", "r"),
        arrayOf("p", "p", "p", "p", "p", "p", "p", "p"),
        arrayOf("", "", "", "", "", "", "", ""),
        arrayOf("", "", "", "", "", "", "", ""),
        arrayOf("", "", "", "", "", "", "", ""),
        arrayOf("", "", "", "", "", "", "", ""),
        arrayOf("P", "P", "P", "P", "P", "P", "P", "P"),
        arrayOf("R", "N", "B", "Q", "K", "B", "N", "R")
    )

    var boardHistory by remember { mutableStateOf(listOf(startingBoard)) }
    var currentMoveIndex by remember { mutableStateOf(0) }
    var boardState by remember { mutableStateOf(startingBoard) }

    // --- CENTRALIZED ENGINE CALL ---
    fun triggerEngineEvaluation() {
        //Grabs the current board position history
        val currentStockfishMoves = moveList.take(currentMoveIndex).joinToString(" ")
        engineOutputText = "Calculating..."

        //Cancl the current job if running
        calculationJob?.cancel()

        calculationJob = coroutineScope.launch(Dispatchers.IO){
            try {
                val isBlackToMove = currentMoveIndex %2 != 0

                //Stop the old plumber, flush the pipe
                stockfish.sendCommand("stop")
                kotlinx.coroutines.delay(50)

                //Read and discard the text
                while (stockfish.isOutputAvailable()){
                    stockfish.readLine()
                }

                //Set the board and begin the calculations
                val posCommand = if (currentStockfishMoves.isEmpty()) "position startpos" else "position startpos moves $currentStockfishMoves"
                stockfish.sendCommand("setoption name MultiPV value $numberOfDesiredMoves")
                stockfish.sendCommand(posCommand)

                stockfish.sendCommand("go infinite")

                //Map to hold the highest depth calculations for each rank
                val latestMoves = mutableMapOf<Int, String>()

                //Listen forever until it's canceled (no longer active)
                while (isActive){
                    val line = stockfish.readLine() ?: break

                    //If bestmove is seen then the stop command was sent
                    if (line.startsWith("bestmove")) break

                    //Parse the info stream lines
                    if (line.startsWith("info") && line.contains("multipv")){
                        val tokens = line.split("\\s+".toRegex())

                        var multiPvId = -1
                        var cpScore = 0
                        var isMateFound = false
                        var mateInValue = 0
                        var moveStr = ""

                        for (i in tokens.indices) {
                            if (tokens[i] == "multipv" && i + 1 < tokens.size) {
                                multiPvId = tokens[i + 1].toIntOrNull() ?: -1
                            }
                            if (tokens[i] == "score" && i + 2 < tokens.size) {
                                if (tokens[i + 1] == "cp") {
                                    cpScore = tokens[i + 2].toIntOrNull() ?: 0
                                    // Invert the score so White is always positive
                                    if (isBlackToMove) cpScore = -cpScore
                                }
                                if (tokens[i + 1] == "mate") {
                                    isMateFound = true
                                    mateInValue = tokens[i + 2].toIntOrNull() ?: 0
                                    if (isBlackToMove) mateInValue = -mateInValue
                                }
                            }
                            if (tokens[i] == "pv" && i + 1 < tokens.size) {
                                moveStr = tokens[i + 1]
                                break
                            }
                        }

                        // 7. UPDATE THE UI: Pass the parsed data to Compose state variables
                        if (multiPvId != -1 && moveStr.isNotEmpty()) {
                            latestMoves[multiPvId] = moveStr

                            // Only update the main eval bar for the #1 best move
                            if (multiPvId == 1) {
                                isMate = isMateFound
                                mateIn = mateInValue
                                currentEval = cpScore / 100f
                            }

                            // Format the text box continuously
                            var newText = ""
                            for (i in 1..numberOfDesiredMoves) {
                                if (latestMoves.containsKey(i)) {
                                    val rawMove = latestMoves[i] ?: ""
                                    // Run it through the human-readable translator!
                                    val humanMove = formatUciToHuman(rawMove, boardState)
                                    newText += "Rank $i: $humanMove\n"
                                }
                            }
                            engineOutputText = newText
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently swallow IO exceptions if the pipe is severed during app shutdown
            }
        }

    }

    Window(
        onCloseRequest = {
            stockfish.stopEngine()
            exitApplication()
        },
        title = "Chess Analyzer",
        state = rememberWindowState(width = 1000.dp, height = 800.dp)
    ) {
        ChessApp(
            boardState = boardState,
            player1 = player1,
            player2 = player2,
            pgnState = pgnState,
            recommendedMoves = engineOutputText,
            currentEval = currentEval,
            isMate = isMate,
            mateIn = mateIn,
            onUploadPgnClick = { isPgnWindowOpen = true },
            nextMove = {
                if (currentMoveIndex < boardHistory.size - 1) {
                    currentMoveIndex++
                    boardState = boardHistory[currentMoveIndex]
                    triggerEngineEvaluation() // Clean, 1-line call!
                }
            },
            previousMove = {
                if (currentMoveIndex > 0) {
                    currentMoveIndex--
                    boardState = boardHistory[currentMoveIndex]
                    triggerEngineEvaluation() // Clean, 1-line call!
                }
            }
        )
    }

    if (isPgnWindowOpen) {
        Window(
            onCloseRequest = { isPgnWindowOpen = false },
            title = "PGN Uploader",
            state = rememberWindowState(width = 400.dp, height = 300.dp)
        ) {
            PgnWindowContent(onSave = { rawText ->
                val lines = rawText.lines()
                val headers = lines.filter { it.trim().startsWith("[") }.joinToString("\n")
                val movesText = lines.filter { !it.trim().startsWith("[") && it.isNotBlank() }.joinToString(" ")
                val sanitizedText = "$headers\n\n$movesText"


                val parser = DataParser(sanitizedText)
                val allMoves = parser.moves
                val newHistory = mutableListOf(startingBoard)
                val uciMoves = mutableListOf<String>()
                var tempBoard = startingBoard

                for (movePair in allMoves) {
                    if (movePair[0].isNotEmpty()) {
                        val result = applyMove(tempBoard, movePair[0], true)
                        tempBoard = result.first; uciMoves.add(result.second); newHistory.add(tempBoard)
                    }
                    if (movePair.size > 1 && movePair[1].isNotEmpty()) {
                        val result = applyMove(tempBoard, movePair[1], false)
                        tempBoard = result.first; uciMoves.add(result.second); newHistory.add(tempBoard)
                    }
                }

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