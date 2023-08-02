package approaches.symbolic.api;

import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;



public class AutocompleteEndpoint extends CachedEndpoint {

    // Compiles the game as far as it goes and returns all completions that start with the input's tail
    public Collection<GenerationNode> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of(new GameNode());

        List<GenerationNode> completions = new ArrayList<>();

        // Assuming we are continuing the ludeme
        if (standardInput.length() > 5 && standardInput.charAt(standardInput.length() - 1) != ' ') {
            for (FractionalCompiler.CompilationState state: FractionalCompiler.compileFraction(standardInput.substring(0, standardInput.lastIndexOf(' ')), symbolMap)) {
                GenerationNode node = state.consistentGame;
                String tail = standardInput.substring(node.root().description().length()).strip();
                completions.addAll(compatibleOptions(node, tail));
            }
        }

        // Assuming we are starting a new ludeme
        for (FractionalCompiler.CompilationState state: cachedCompilation) {
            GenerationNode node = state.consistentGame;
            String tail = standardInput.substring(node.root().description().length()).strip();
            completions.addAll(compatibleOptions(node, tail));
        }

        return completions;
    }

    // TODO make it consider possibilities bellow the top of the stack
    //
    public List<GenerationNode> compatibleOptions(GenerationNode node, String tail) {

        List<GenerationNode> completions = new ArrayList<>();

        for (GenerationNode option: node.nextPossibleParameters(symbolMap, null, false, true)) {
            assert !(option instanceof EmptyNode);

            // Verify whether the option is compatible with the tail
            if (option instanceof PrimitiveNode primitiveOption) {

                String value = tail;
                if (option.symbol().label != null) {
                    if (!tail.startsWith(' ' + option.symbol().label))
                        continue;

                    value = tail.substring(option.symbol().label.length() + 1);
                }

                boolean valid = switch (primitiveOption.getType()) {
                    case INT -> {
                        if (value.equals("-")) yield true;
                        try {
                            Integer.parseInt(value);
                            yield true;
                        } catch (NumberFormatException ignored) {
                            yield false;
                        }
                    }
                    case FLOAT -> {
                        if (value.equals("-")) yield true;
                        try {
                            Float.parseFloat(value);
                            yield true;
                        } catch (NumberFormatException ignored) {
                            yield false;
                        }
                    }
                    case DIM -> {
                        try {
                            yield Integer.parseInt(value) >= 0;
                        } catch (NumberFormatException ignored) {
                            yield false;
                        }
                    }
                    case STRING -> value.startsWith("\"")
                            && value.substring(1).indexOf('"') == -1; // Todo accept \" if we ever need to support metadata
                    case BOOLEAN -> "True".startsWith(value) || "False".startsWith(value);
                };

                System.out.println("value:" + value + " valid:" + valid);

                if (!value.isEmpty() && !valid)
                    continue;

            }else if (option instanceof EndOfClauseNode) {
                if (!tail.isEmpty())
                    continue;
            } else {
                if (!option.description().startsWith(tail))
                    continue;
            }


            GenerationNode newNode = node.copyUp();
            newNode.addParameter(option);

            completions.add(option);
        }

        return completions;
    }

    @Override
    String respond() {
        String standardInput = standardize(rawInput);
        StringBuilder sb = new StringBuilder();
        HashSet<String> completions = new HashSet<>();
        for (GenerationNode option : autocomplete(standardInput)) {
            //assert option.root().description().startsWith(standardInput);
            String completion;

            if (option instanceof PrimitiveNode) {
                completion = option.description();
            }
            else {
                String description = option.root().description();
                if (description.length() < standardInput.length()) continue;
                completion = description.substring(standardInput.length());
            }

            if (!completions.contains(completion)) {
                completions.add(completion);
                sb.append(completion).append("|").append(option.symbol().grammarLabel()).append("||");
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
