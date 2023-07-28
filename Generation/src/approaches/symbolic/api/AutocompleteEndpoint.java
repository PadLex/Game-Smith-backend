package approaches.symbolic.api;

import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint extends CachedEndpoint {

    public Collection<GenerationNode> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of(new GameNode());

        List<GenerationNode> completions = new ArrayList<>();

        for (FractionalCompiler.CompilationState state: cachedCompilation) {
            GenerationNode node = state.consistentGame;
            String prefix = standardInput.substring(node.root().description().length()).strip();
            completions.addAll(compatibleOptions(node, prefix));
        }

        return completions;
    }

    // TODO make it consider possibilities bellow the top of the stack
    public List<GenerationNode> compatibleOptions(GenerationNode node, String prefix) {

        List<GenerationNode> completions = new ArrayList<>();

        for (GenerationNode option: node.nextPossibleParameters(symbolMap, null, false, true)) {
            assert !(option instanceof EmptyNode);
//            System.out.println(option + " - " + prefix);
            if (option instanceof EndOfClauseNode) {
                if (prefix.length() > 0)
                    continue;
            }else if (!option.description().startsWith(prefix))
                continue;

            GenerationNode newNode = node.copyUp();
            newNode.addParameter(option);

            completions.add(newNode);
        }

        return completions;
    }

    @Override
    String respond() {
        String standardInput = standardize(rawInput);
        StringBuilder sb = new StringBuilder();
        HashSet<String> completions = new HashSet<>();
        for (GenerationNode node : autocomplete(standardInput)) {
            assert node.root().description().startsWith(standardInput);
            String completion = node.root().description().substring(standardInput.length());
            if (!completions.contains(completion)) {
                completions.add(completion);
                sb.append(completion).append("|").append(node.symbol().grammarLabel()).append("||");
            }
        }
        if (sb.length() > 2)
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public static void main(String[] args) {
        new AutocompleteEndpoint().start();
    }
}
