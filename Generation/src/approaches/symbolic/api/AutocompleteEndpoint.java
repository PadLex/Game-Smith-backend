package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint extends CachedEndpoint {

    public Collection<GeneratorNode> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of(new GameNode());

        Map<String, GeneratorNode> completions = new HashMap<>();

        for (FractionalCompiler.CompilationState state: cachedCompilation) {
            GeneratorNode node = state.consistentGame;
            String prefix = standardInput.substring(node.root().description().length()).strip();
            for (GeneratorNode option: compatibleOptions(node, prefix)){
                completions.put(option.symbol().path(), option);
            }
        }

        return completions.values();
    }

    // TODO make it consider possibilities bellow the top of the stack
    public List<GeneratorNode> compatibleOptions(GeneratorNode node, String prefix) {

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

    @Override
    String respond() {
        String standardInput = standardize(rawInput);
        StringBuilder sb = new StringBuilder();
        for (GeneratorNode node : autocomplete(standardInput)) {
            assert node.root().description().startsWith(standardInput);
            String completion = node.root().description().substring(standardInput.length());
            sb.append(completion).append("|").append(node.symbol().grammarLabel()).append("||");
        }
        if (sb.length() > 2)
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public static void main(String[] args) {
        new AutocompleteEndpoint().start();
    }
}
