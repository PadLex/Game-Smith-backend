package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.ArrayNode;
import approaches.symbolic.nodes.EmptyNode;
import approaches.symbolic.nodes.EndOfClauseNode;
import approaches.symbolic.nodes.GeneratorNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import static approaches.symbolic.FractionalCompiler.compileFraction;
import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint {

    static class Completion {
        final String completion;
        final String description;

        public Completion(String completion, String description) {
            this.completion = completion;
            this.description = description;
        }
    }

    public static List<Completion> autocomplete(String standardInput, SymbolMapper symbolMapper) {
        if ("(gam".startsWith(standardInput))
            return List.of(new Completion("(game", "game.Game"));

        Stack<FractionalCompiler.CompilationState>  partialCompilation = FractionalCompiler.compileFraction(standardInput, symbolMapper);

        List<Completion> completions = new ArrayList<>();

        for (FractionalCompiler.CompilationState state: partialCompilation) {
            GeneratorNode node = state.consistentGame;
            String trailing = standardInput.substring(node.root().description().length()).strip();
            completions.addAll(buildCompletions(node, trailing, symbolMapper));
        }

        return completions;
    }

    // TODO make it consider possibilities bellow the top of the stack
    public static List<Completion> buildCompletions(GeneratorNode node, String trailing, SymbolMapper symbolMapper) {

        List<Completion> completions = new ArrayList<>();

        for (GeneratorNode option: node.nextPossibleParameters(symbolMapper, null, false, true)) {
            assert !(option instanceof EmptyNode);
            if (!option.description().startsWith(trailing))
                continue;

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
            String input = sc.nextLine();
            input = input.replace("\\n", "\n");
            for (Completion completion : autocomplete(standardize(input), symbolMapper)) {
                System.out.print(completion.completion + "|" + completion.description + "||");
            }
            System.out.println();
        }
        sc.close();
    }
}
