import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataParser {

    private String event, date, white, black, result, blackElo, whiteElo;
    private String[][] moves;

    public DataParser(String data) {
        if (data == null || data.isEmpty()) return;

        Scanner scanner = new Scanner(data);
        List<String[]> moveList = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            if (
                    line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']'
            ) {
                // Better way: Find the first and last quotes
                int firstQuote = line.indexOf("\"");
                int lastQuote = line.lastIndexOf("\"");

                System.out.println(firstQuote + " " + lastQuote);

                if (firstQuote != -1 && lastQuote > firstQuote) {
                    String key = line.substring(1, firstQuote).trim();
                    String value = line.substring(firstQuote + 1, lastQuote);

                    switch (key) {
                        case "Event":
                            event = value;
                            break;
                        case "White":
                            white = value;
                            break;
                        case "Black":
                            black = value;
                            break;
                        case "Date":
                            date = value;
                            break;
                        case "Result":
                            result = value;
                            break;
                        case "WhiteElo":
                            whiteElo = value;
                            break;
                        case "BlackElo":
                            blackElo = value;
                            break;
                    }
                }
            } else {
                //Parse moves
                String[] tokens = line.split("\\s+");

                for (int i = 0; i < tokens.length; i++) {
                    String t = tokens[i];
                    // Skip move numbers (1.) and results (1-0)
                    if (
                            t.contains(".") || t.matches("1-0|0-1|1/2-1/2|\\*")
                    ) continue;

                    String whiteMove = t;
                    String blackMove = "";

                    if (i + 1 < tokens.length) {
                        String next = tokens[i + 1];
                        if (
                                !next.contains(".") &&
                                        !next.matches("1-0|0-1|1/2-1/2|\\*")
                        ) {
                            blackMove = next;
                            i++;
                        }
                    }
                    moveList.add(new String[] { whiteMove, blackMove });
                }
            }
        }
        scanner.close();
        // Convert the dynamic list to your 2D array
        this.moves = moveList.toArray(new String[0][0]);
    }

    public String getEvent() {
        return event;
    }

    public String getDate() {
        return date;
    }

    public String getWhite() {
        return white;
    }

    public String getBlack() {
        return black;
    }

    public String getResult() {
        return result;
    }

    public String getBlackElo() {
        return blackElo;
    }

    public String getWhiteElo() {
        return whiteElo;
    }

    public String[][] getMoves() {
        return moves;
    }
}
