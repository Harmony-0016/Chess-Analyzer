import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.PublicKey;

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
     * Starts the C++ program in the background.
     * @return true if the engine was successfully launched
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

    /**
     * @author Liam Guillemette
     * @param command: the message that is sent to stockfish
     * @prerequisite: Message must be formatted
     */
    public void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error sending command: " + e.getMessage());
        }
    }

    /**
     * Reads a single line of text from the engine stream.
     * @author Liam Guillemette
     */
    public String readLine() throws IOException {
        if (processReader != null) {
            return processReader.readLine();
        }
        return null;
    }

    /**
     * Checks if there is text waiting in the pipe without blocking the thread
     * @return true if text is waiting, false otherwise
     */
    public boolean isOutputAvailable() throws IOException {
        return processReader != null && processReader.ready();
    }

    public void stopEngine(){
        try{
            sendCommand("quit");
        } catch (Exception e){
            //ignore if it fails
        } finally {
            try {
                if (processReader != null) processReader.close();
                if (writer != null) writer.close();
            } catch (IOException e){
                //leave it away
            }

            //Destroy the process
            if (engineProcess != null){
                engineProcess.destroy();
            }
        }
    }
}
