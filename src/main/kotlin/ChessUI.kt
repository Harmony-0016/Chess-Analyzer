import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            //Eval Bar
            EvaluationBar(eval = currentEval, isMate = isMate, mateIn = mateIn)

            //The Left side
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = player2, color = ChessLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ChessBoard(boardState)
                Text(text = player1, color = ChessLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.width(300.dp).height(50.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = previousMove,
                        colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = ChessLight),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Previous") }

                    Button(
                        onClick = nextMove,
                        colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = ChessLight),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Next") }
                }
            }

            //The right side of the UI
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Content: $pgnState", color = ChessLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Button(
                    onClick = onUploadPgnClick,
                    colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = ChessLight),
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
                    Text(text = recommendedMoves, color = ChessLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

//Sub-composables
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
                            Text(text = getPieceSymbol(piece), fontSize = 40.sp, color = DarkBackground)
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
        Text("Paste your PGN data below:", color = DarkBackground)
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
            colors = ButtonDefaults.buttonColors(backgroundColor = ChessGreen, contentColor = ChessLight),
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

    Box(modifier = Modifier.width(24.dp).height(400.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, DarkBackground, RoundedCornerShape(4.dp))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f - whitePercentage + 0.001f).background(EvalDark),
                contentAlignment = Alignment.TopCenter
            ) {
                if (eval < 0 || (isMate && mateIn < 0)) {
                    Text(text = evalText, color = ChessLight, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().weight(whitePercentage + 0.001f).background(ChessLight),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (eval >= 0 || (isMate && mateIn > 0)) {
                    Text(text = evalText, color = DarkBackground, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}