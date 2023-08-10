package approaches.symbolic;

import approaches.symbolic.nodes.GameNode;
import compiler.Compiler;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static approaches.symbolic.FractionalCompiler.compileComplete;
import static approaches.symbolic.FractionalCompiler.standardize;

public class Dataset {
    static void buildDataset(SymbolMap symbolMap, int limit) throws IOException {
        List<String> skip = List.of("Kriegspiel (Chess).lud", "Throngs.lud", "Tai Shogi.lud", "Taikyoku Shogi.lud", "Yonin Seireigi.lud", "Yonin Shogi.lud"); // "To Kinegi tou Lagou.lud"

        String gamesRoot = "./Common/res/lud/board";
        String outputRoot = "./expanded";
        Path outputRootPath = Paths.get(outputRoot);

        // Create the output root directory if it doesn't exist
        if (!Files.exists(outputRootPath)) {
            Files.createDirectory(outputRootPath);
        }

        List<Path> paths = Files.walk(Paths.get(gamesRoot)).filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".lud")).sorted().limit(limit).toList();
        int count = 0;
        for (Path path : paths) {
            String gameStr = Files.readString(path);
            count++;

            if (gameStr.contains("match")) {
                System.out.println("Skipping match " + path.getFileName());
                continue;
            }

            if (skip.contains(path.getFileName().toString())) {
                System.out.println("Skipping skip" + path.getFileName());
                continue;
            }

            System.out.println("\nLoading " + path.getFileName() + " (" + (count + 1) + " of " + paths.size() + " games)");

            Description description = new Description(gameStr);
            final UserSelections userSelections = new UserSelections(new ArrayList<>());
            final Report report = new Report();
            Parser.expandAndParse(description, userSelections, report, true, false);
            String expandedDescription = standardize(description.expanded());

            Path relativePath = Paths.get(gamesRoot).relativize(path.getParent());
            Path outputPathDirectory = Paths.get(outputRoot, relativePath.toString());

            // Create directories as needed
            if (!Files.exists(outputPathDirectory)) {
                Files.createDirectories(outputPathDirectory);
            }

            Path outputPath = outputPathDirectory.resolve(path.getFileName().toString());
            Files.writeString(outputPath, expandedDescription);
        }
    }

    public static void main(String[] args) throws IOException {
        SymbolMap symbolMap = new CachedMap();
        buildDataset(symbolMap, 2000);
    }
}
