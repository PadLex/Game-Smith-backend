package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.GeneratorNode;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import static approaches.symbolic.FractionalCompiler.compileFraction;
import static approaches.symbolic.FractionalCompiler.standardize;

public class ConstraintEndpoint extends AutocompleteEndpoint {
    public static List<String> constrain(String standardInput, SymbolMapper symbolMapper) {
        if ("(gam".startsWith(standardInput))
            return List.of("(game".substring(standardInput.length()));

        Stack<FractionalCompiler.CompilationState> partialCompilation = FractionalCompiler.compileFraction(standardInput, symbolMapper);
        GeneratorNode node = partialCompilation.peek().consistentGame;

        String trailing = standardInput.substring(node.root().description().length()).strip();

        return buildCompletions(node, trailing, symbolMapper).stream().map(c -> c.completion.substring(trailing.length())).toList();
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        SymbolMapper symbolMapper = new CachedMapper();

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            input = input.replace("\\n", "\n");
            for (String completion : constrain(standardize(input), symbolMapper)) {
                System.out.print(completion + "||");
            }
            System.out.println();
        }
        sc.close();
    }
}
