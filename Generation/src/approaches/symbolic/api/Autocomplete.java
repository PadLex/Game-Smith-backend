package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.DescriptionParser;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.ArrayNode;
import approaches.symbolic.nodes.EmptyNode;
import approaches.symbolic.nodes.EndOfClauseNode;
import approaches.symbolic.nodes.GeneratorNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static approaches.symbolic.DescriptionParser.compilePartialDescription;
import static approaches.symbolic.DescriptionParser.standardize;


public class Autocomplete {

    static class Completion {
        final String completion;
        final String description;

        public Completion(String completion, String description) {
            this.completion = completion;
            this.description = description;
        }
    }

    // TODO make it consider possibilities bellow the top of the stack
    public static List<Completion> autocomplete(String rawInput, SymbolMapper symbolMapper) {
        String standardInput = standardize(rawInput);
        if (standardInput.isEmpty())
            return List.of(new Completion("Game", "game.Game"));
        if (standardInput.length() < 5)
            return new ArrayList<>();
        DescriptionParser.PartialCompilation partialCompilation = compilePartialDescription(standardInput, symbolMapper);
        GeneratorNode node = partialCompilation.consistentGames.peek().consistentGame;
        List<Completion> completions = new ArrayList<>();

        if (standardInput.chars().filter(c -> c == ' ').count() != node.root().description().chars().filter(c -> c == ' ').count())
            return completions;

        for (GeneratorNode option: node.nextPossibleParameters(symbolMapper, null, false, true)) {
            assert !(option instanceof EmptyNode);

            GeneratorNode newNode = node.copyUp();
            newNode.addParameter(option);

            String description = option.symbol().path();
            String completion;

            if (option instanceof EndOfClauseNode) {
                description = "End of clause";
                completion = option.parent() instanceof ArrayNode ? "}" : ")";
            } else {
                completion = option.description();
            }

            completions.add(new Completion(completion, description));
        }

        return completions;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        SymbolMapper symbolMapper = new CachedMapper();

        while (sc.hasNextLine()) {
            for (Completion completion : autocomplete(sc.nextLine(), symbolMapper)) {
                System.out.print(completion.completion + "|" + completion.description + "||");
            }
            System.out.println();
        }
        sc.close();
    }
}
