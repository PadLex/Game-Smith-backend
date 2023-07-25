package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint {

    public static Collection<GeneratorNode> autocomplete(String standardInput, SymbolMapper symbolMapper) {
        if ("(gam".startsWith(standardInput))
            return List.of(new GameNode());

        Stack<FractionalCompiler.CompilationState>  partialCompilation = FractionalCompiler.compileFraction(standardInput, symbolMapper);

        Map<String, GeneratorNode> completions = new HashMap<>();

        for (FractionalCompiler.CompilationState state: partialCompilation) {
            GeneratorNode node = state.consistentGame;
            String prefix = standardInput.substring(node.root().description().length()).strip();
            for (GeneratorNode option: compatibleOptions(node, prefix, symbolMapper)){
                completions.put(option.symbol().path(), option);
            }
        }

        return completions.values();
    }

    // TODO make it consider possibilities bellow the top of the stack
    public static List<GeneratorNode> compatibleOptions(GeneratorNode node, String prefix, SymbolMapper symbolMapper) {

        List<GeneratorNode> completions = new ArrayList<>();

        for (GeneratorNode option: node.nextPossibleParameters(symbolMapper, null, false, true)) {
            assert !(option instanceof EmptyNode);
//            System.out.println(option + " - " + prefix);
            if (option instanceof EndOfClauseNode) {
                if (prefix.length() > 0)
                    continue;
            }else if (!option.description().startsWith(prefix))
                continue;

            GeneratorNode newNode = node.copyUp();
            newNode.addParameter(option);

            completions.add(newNode);
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
            String standardInput = standardize(input);
            for (GeneratorNode node : autocomplete(standardInput, symbolMapper)) {
                assert node.root().description().startsWith(standardInput);
                String completion = node.root().description().substring(standardInput.length());
                System.out.print(completion + "|" + node.symbol().grammarLabel() + "||");
            }
            System.out.println();
        }
        sc.close();
    }
}
