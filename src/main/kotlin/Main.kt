import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() = application {
    //App State
    var isPgnWindowOpen by remember { mutableStateOf(false) }
    var player1 by remember { mutableStateOf("White Player: ?") }
    var player2 by remember { mutableStateOf("Black Player: ?") }
    var pgnState by remember { mutableStateOf("Empty") }

    //Engine State
    var currentEval by remember { mutableStateOf(0f) }
    var isMate by remember { mutableStateOf(false) }
    var mateIn by remember { mutableStateOf(0) }
    var engineOutputText by remember { mutableStateOf("Upload a PGN to begin.") }
    val numberOfDesiredMoves = 3
    var moveList by remember { mutableStateOf(emptyList<String>()) }

    //Thread Management
    val coroutineScope = rememberCoroutineScope()
    var calculationJob by remember { mutableStateOf<Job?>(null) } // Safety toggle to prevent lag!

    val stockfish = remember {
        val engine = StockFishEngine("stockfish_x86-64-avx512.exe")
        engine.startEngine()
        engine
    }

    //Board State
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

    //Engine Call
    fun triggerEngineEvaluation() {
        //Grabs the current board position history
        val currentStockfishMoves = moveList.take(currentMoveIndex).joinToString(" ")
        engineOutputText = "Calculating..."

        //Cancel the current job if running
        calculationJob?.cancel()

        calculationJob = coroutineScope.launch(Dispatchers.IO){
            try {
                val isBlackToMove = currentMoveIndex %2 != 0

                //Stop the old plumber, flush the pipe
                stockfish.sendCommand("stop")
                kotlinx.coroutines.delay(50)

                //Read and discard the text
                while (stockfish.isOutputAvailable){
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


                        if (multiPvId != -1 && moveStr.isNotEmpty()) {
                            latestMoves[multiPvId] = moveStr

                            //Update the eval par for the best move
                            if (multiPvId == 1) {
                                isMate = isMateFound
                                mateIn = mateInValue
                                currentEval = cpScore / 100f
                            }

                            //Format the text box continuously
                            var newText = ""
                            for (i in 1..numberOfDesiredMoves) {
                                if (latestMoves.containsKey(i)) {
                                    val rawMove = latestMoves[i] ?: ""
                                    val humanMove = formatUciToHuman(rawMove, boardState)
                                    newText += "Rank $i: $humanMove\n"
                                }
                            }
                            engineOutputText = newText
                        }
                    }
                }
            } catch (e: Exception) {
                //Silently swallow IO exceptions if the pipe is severed during app shutdown
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