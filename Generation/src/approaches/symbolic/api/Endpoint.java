package approaches.symbolic.api;

import supplementary.experiments.eval.EvalGames;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public abstract class Endpoint {
    private static final PrintStream systemOut = System.out;
    private static final PrintStream systemErr = System.err;

    String rawInput;

    String logFile = "cached-log.txt";
    boolean logToFile = true;
    String oldLogId;

    abstract String respond();

    void start() {
        // Override System.out and System.err to log everything written to them
        if (logToFile) {
            System.setOut(createLoggingPrintStream("Print"));
            System.setErr(createLoggingPrintStream("Error"));
        }

        EvalGames.debug = false;

        Scanner sc = new Scanner(System.in);
        systemOut.println("Ready");

        while (sc.hasNextLine()) {
            rawInput = sc.nextLine().replace("\\n", "\n");

            String response;
            try {
                response = respond();
            } catch (Exception e) {
                response = "";
                System.err.println("Crashed: " + e.getMessage());
                e.printStackTrace();
            }

            // Output
            systemOut.println(response.replace("\n", "\\n"));
        }
        sc.close();
    }

    private PrintStream createLoggingPrintStream(String title) {
        return new PrintStream(new OutputStream() {
            final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;
                if (c == '\n') {
                    log(title, buffer.toString());
                    buffer.setLength(0);
                } else {
                    buffer.append(c);
                }
            }
        });
    }

    public void log(String title, String message) {
        if (!logToFile) {
            System.out.println(title + ":" + message);
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            String id = this.getClass().getSimpleName() + " - " + title + rawInput;
            if (!id.equals(oldLogId)) {
                oldLogId = id;
                writer.newLine();
                writer.newLine();
                writer.write(this.getClass().getSimpleName() + " - " + System.currentTimeMillis() + " - " + title + ":");
                writer.newLine();
                writer.write("Raw Input:");
                writer.newLine();
                writer.write(rawInput);
                writer.newLine();
                writer.write("Message:");
                writer.newLine();
                writer.write(message);
            } else {
                writer.newLine();
                writer.write(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
