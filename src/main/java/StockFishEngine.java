import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockFishEngine {

    private Process engineProcess;
    private BufferedReader processReader;
    private OutputStreamWriter writer;
    private final String PATH;

    /**
     * Passes the path to the Stockfish Executable
     * @param path The path to the Stockfish executable
     */
    public StockFishEngine(String path) {
        this.PATH = path;
    }

    /**
     *
     */
    public boolean startEngine(){
        try {
            ProcessBuilder pb = new ProcessBuilder(PATH);
            engineProcess = pb.start();
            processReader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            writer = new OutputStreamWriter(engineProcess.getOutputStream());

            sendCommand("uci");

            String line;
            while ((line = processReader.readLine()) != null) {
                System.out.println(line);
                if (line.equals("uciok")) {
                    break;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setMultiPv(int lines) {
        sendCommand("setoption name MultiPV value " + lines);
    }

    public void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush(); // CRITICAL: Forces the text down the pipe
        } catch (IOException e) {
            System.err.println("Error sending command: " + e.getMessage());
        }
    }

    /**
     * Gets the top N moves from the engine.
     * @param moves A space-separated string of moves (e.g., "e2e4 e7e5")
     * @param waitTime Milliseconds to think
     * @param numLines How many alternative moves to return
     * @return A list of EngineMove objects, ranked from best to worst.
     */
    public List<EngineMove> getTopMoves(String moves, int waitTime, int numLines) {

        //Use mpv -- Multi Principal Variation
        sendCommand("setoption name MultiPV value " + numLines);

        //Give it the starting moves, then give it the wait time
        if (moves.isEmpty()) {
            sendCommand("position startpos");
        } else {
            sendCommand("position startpos moves " + moves);
        }
        sendCommand("go movetime " + waitTime);

        //Map to constantly overwrite older depths with newer depths
        Map<Integer, EngineMove> latestMoves = new HashMap<>();

        try {
            String line;
            while ((line = processReader.readLine()) != null) {

                //If it's an info line containing multiple variations
                if (line.startsWith("info") && line.contains("multipv")) {
                    String[] tokens = line.split(" ");

                    int multiPvId = -1;
                    int cpScore = 0;
                    boolean isMate = false;
                    int mateIn = 0;
                    String moveStr = "";

                    //Loop through the tokens to find our data
                    for (int i = 0; i < tokens.length; i++) {
                        if (tokens[i].equals("multipv")) {
                            multiPvId = Integer.parseInt(tokens[i + 1]);
                        }
                        else if (tokens[i].equals("score")) {
                            if (tokens[i + 1].equals("cp")) {
                                cpScore = Integer.parseInt(tokens[i + 2]);
                            } else if (tokens[i + 1].equals("mate")) {
                                isMate = true;
                                mateIn = Integer.parseInt(tokens[i + 2]);
                            }
                        }
                        else if (tokens[i].equals("pv")) {
                            //Token after pv is the desired
                            moveStr = tokens[i + 1];
                            break;
                        }
                    }

                    //Save or overwrite this ID in our map
                    if (multiPvId != -1 && !moveStr.isEmpty()) {
                        latestMoves.put(multiPvId, new EngineMove(moveStr, cpScore, isMate, mateIn));
                    }
                }
                //Finish at bestmove
                else if (line.startsWith("bestmove")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from Stockfish: " + e.getMessage());
        }

        // Convert our Map into a clean, ordered List to send back to Kotlin
        List<EngineMove> finalMoves = new ArrayList<>();
        for (int i = 1; i <= numLines; i++) {
            if (latestMoves.containsKey(i)) {
                finalMoves.add(latestMoves.get(i));
            }
        }

        return finalMoves;
    }

    public void stopEngine() {
        try {
            sendCommand("quit");
            processReader.close();
            writer.close();
            engineProcess.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
