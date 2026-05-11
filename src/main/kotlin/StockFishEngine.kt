import java.io.*

class StockfishEngine(private val path: String) {
    // 1. Corrected typos: bufferedReader and bufferedWriter
    private val process = ProcessBuilder(path).start()
    private val reader = process.inputStream.bufferedReader()
    private val writer = process.outputStream.bufferedWriter()

    init {
        sendCommand("uci")
    }

    // 2. Ensure parameter name (command) matches the usage in writer.write
    fun sendCommand(command: String) {
        writer.write("$command\n")
        writer.flush()
    }

    fun getBestMove(fen: String): String? {
        sendCommand("position fen $fen")
        sendCommand("go movetime 1000")

        // 3. Robust way to read until we find "bestmove"
        var line: String?
        while (true) {
            line = reader.readLine() ?: break
            if (line.startsWith("bestmove")) {
                return line.split(" ")[1]
            }
        }
        return null
    }
}