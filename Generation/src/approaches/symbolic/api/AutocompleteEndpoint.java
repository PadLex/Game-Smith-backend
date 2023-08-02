package approaches.symbolic.api;

import approaches.symbolic.CachedMap;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMap;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;



public class AutocompleteEndpoint {

    String currentStandardInput;
    List<FractionalCompiler.CompilationState> previousCompilation;
    List<FractionalCompiler.CompilationState> currentCompilation;
    SymbolMap symbolMap;

    // Compiles the game as far as it goes and returns all completions that start with the input's tail
    public Collection<GenerationNode> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of(new GameNode());

        List<GenerationNode> completions = new ArrayList<>();

        // Assuming we are continuing the ludeme
//        if (standardInput.length() > 5 && standardInput.charAt(standardInput.length() - 1) != ' ') {
//            for (FractionalCompiler.CompilationState state: previousCompilation) {
//                GenerationNode node = state.consistentGame;
//                String tail = standardInput.substring(node.root().description().length()).strip();
//                completions.addAll(compatibleOptions(node, tail).stream().filter(n -> !n.description().equals(tail)).toList());
//            }
//        }

        // Assuming we are starting a new ludeme
        for (FractionalCompiler.CompilationState state: currentCompilation) {
            GenerationNode node = state.consistentGame;
            String tail = standardInput.substring(node.root().description().length());
            completions.addAll(compatibleOptions(node, tail));
        }

        return completions;
    }

    // TODO make it consider possibilities bellow the top of the stack
    //
    public List<GenerationNode> compatibleOptions(GenerationNode node, String tail) {

        System.out.println(node.root().nodeCount() + " - " + node.root().description());
        System.out.println(node.root().nodeCount() + " - " + node.root());
        System.out.println(tail);

        List<GenerationNode> completions = new ArrayList<>();

        for (GenerationNode option: node.nextPossibleParameters(symbolMap, null, false, true)) {
            assert !(option instanceof EmptyNode);

            System.out.println("option " + option);

            // Verify whether the option is compatible with the tail
            if (option instanceof PrimitiveNode primitiveOption) {

                String value = tail;

                if (option.symbol().label != null) {
                    if (!tail.startsWith(' ' + option.symbol().label))
                        continue;

                    value = tail.substring(option.symbol().label.length() + 1);
                } else if (!tail.isEmpty() && value.charAt(0) == ' ') {
                    value = value.substring(1);
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
                        if (value.endsWith(".")) value = value.substring(0, value.length() - 1);
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

//                System.out.println("value:" + value + " valid:" + valid);

                if (value.isEmpty() || valid) {
                    GenerationNode newNode = node.copyUp();
                    if (!value.isEmpty())
                        option = new ContinuedPrimitive(primitiveOption);
                    newNode.addParameter(option);
                    completions.add(option);
                }

            }else if (option instanceof EndOfClauseNode) {
                if (tail.isEmpty()) {
                    GenerationNode newNode = node.copyUp();
                    newNode.addParameter(option);
                    completions.add(option);
                }
            } else {
                if (option.description().startsWith(tail.strip())) {
                    GenerationNode newNode = node.copyUp();
                    newNode.addParameter(option);
                    completions.add(option);
                }
            }

        }

        return completions;
    }

    String respond(String standardInput) {
        StringBuilder sb = new StringBuilder();
        HashSet<String> completions = new HashSet<>();
        for (GenerationNode option : autocomplete(standardInput)) {
            //assert option.root().description().startsWith(standardInput);
            String completion;

            if (option instanceof PrimitiveNode) {
                completion = option.description();
                if (!standardInput.endsWith(" ") && !(option instanceof ContinuedPrimitive))
                    completion = " " + completion;

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

    void updateCache(String standardInput) {
//        // Reset the cache if the input is not a continuation of the previous input
//        if (currentStandardInput != null && !standardInput.startsWith(currentStandardInput)) {
//            previousCompilation = null;
//            currentCompilation = null;
//        }
//        // Initialize the cache if it is empty
//        if (currentCompilation == null) {
//            currentCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
//            if (standardInput.length() > 5)
//                previousCompilation = FractionalCompiler.compileFraction(standardInput.substring(0, standardInput.lastIndexOf(' ')), symbolMap);
//            else
//                previousCompilation = null;
//        } else {
//            Stack<FractionalCompiler.CompilationState> newCompilation = FractionalCompiler.compileFraction(standardInput, currentCompilation, symbolMap);
//            if (!newCompilation.peek().consistentGame.description().equals(currentCompilation.peek().consistentGame.root().description())) {
//                previousCompilation = currentCompilation;
//                currentCompilation = newCompilation;
//            }
//        }

        currentCompilation = FractionalCompiler.compileFraction(standardInput, symbolMap);
        if (standardInput.length() > 5)
            previousCompilation = FractionalCompiler.compileFraction(standardInput.substring(0, standardInput.lastIndexOf(' ')), symbolMap);
    }

    void start() {
        Scanner sc = new Scanner(System.in);

        symbolMap = new CachedMap();

        System.out.println("Ready");

        while (sc.hasNextLine()) {
            // Input
            String standardInput = standardize(sc.nextLine().replace("\\n", "\n"));
            updateCache(standardInput);
            // Output
            System.out.println(respond(standardInput).replace("\n", "\\n"));
        }
        sc.close();
    }
//FractionalCompiler.compileFraction(standardInput.substring(0, standardInput.lastIndexOf(' ')), symbolMap)
    public static void main(String[] args) {
        new AutocompleteEndpoint().start();
    }

    class ContinuedPrimitive extends PrimitiveNode {

        public ContinuedPrimitive(PrimitiveNode primitiveNode) {
            super(primitiveNode.symbol(), primitiveNode.parent());
            setValue(primitiveNode.value());
        }

        @Override
        public String toString() {
            return "Continued" + super.toString();
        }

        @Override
        public String description() {
            return "Continued" + super.description();
        }
    }
}
