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



@Composable
@Preview
fun app(onUploadPgnClick: () -> Unit){
    //Make the initial board.
    var boardState by remember {
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
                            onClick = { previousMove() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF769656),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = { nextMove() },
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
        // This Composable is "Stateless", so Compose skips it during recomposition
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

fun nextMove(){

}

fun previousMove(){

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


    //Main Window
    Window(
        onCloseRequest = ::exitApplication,
        title = "Chess Analyzer",
        state = rememberWindowState(width = 1000.dp, height = 800.dp)
    ) {
        app(onUploadPgnClick = { isPgnWindowOpen = true})
    }

    //Shows if PGN window is open
    if (isPgnWindowOpen) {
        Window(
            onCloseRequest = { isPgnWindowOpen = false }, // Close this window only
            title = "PGN Uploader",
            state = rememberWindowState(width = 400.dp, height = 300.dp)
        ) {
            PgnWindowContent(onSave = {
                rawText ->
                val parser = DataParser(rawText)
                println("Successfully loaded game by: ${parser.white}")
                isPgnWindowOpen = false
            })
        }
    }
}