package approaches.symbolic.api;

import approaches.symbolic.CachedMap;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMap;

import java.util.List;
import java.util.Scanner;

import static approaches.symbolic.FractionalCompiler.standardize;

public abstract class CachedEndpoint {
    SymbolMap symbolMap = new CachedMap();
    FractionalCompiler.CompilationCheckpoint cachedCompilation;
    String rawInput;
    abstract String respond();

    void updateCache(String input) {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logggg.txt", true))) {
//            writer.write("Input:\n" + input);
//            writer.newLine(); // Add a newline after each log message (optional)
//        } catch (IOException e) {}

        String standardInput = standardize(input);

//        if (cachedCompilation == null || cachedCompilation.size() == 0) {
//            cachedCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
//        } else {
//            String cachedDescription = cachedCompilation.peek().consistentGame.root().description();
//            if (!standardInput.equals(cachedDescription)) {
//                if (standardInput.startsWith(cachedDescription))
//                    cachedCompilation = FractionalCompiler.compileFraction(standardInput, cachedCompilation, symbolMap);
//                else
//                    cachedCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
//            }
//        }

        cachedCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);


        rawInput = input;
    }

    void start() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        while (sc.hasNextLine()) {
//            long start = System.currentTimeMillis();
            // Input
            updateCache(sc.nextLine().replace("\\n", "\n"));

//            try {
//                respond();
//            } catch (Exception e) {
//                try (BufferedWriter writer = new BufferedWriter(new FileWriter("logggg.txt", true))) {
//                    writer.write("Error:\n" + e.getMessage());
//                    writer.newLine(); // Add a newline after each log message (optional)
//                } catch (IOException ie) {}
//            }
//
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter("logggg.txt", true))) {
//                writer.write("Output:\n" + respond());
//                writer.newLine(); // Add a newline after each log message (optional)
//            } catch (IOException e) {}
            // Output
            System.out.println(respond().replace("\n", "\\n"));
//            long end = System.currentTimeMillis();
//            System.out.println(end - start + "ms");
        }
        sc.close();
    }
}
