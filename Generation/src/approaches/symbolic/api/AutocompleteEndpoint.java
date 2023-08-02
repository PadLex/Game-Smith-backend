package approaches.symbolic.api;

import approaches.symbolic.CachedMap;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMap;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint {

    String currentStandardInput;
    FractionalCompiler.CompilationCheckpoint previousCompilation;
    FractionalCompiler.CompilationCheckpoint currentCompilation;
    SymbolMap symbolMap;

    // Compiles the game as far as it goes and returns all completions that start with the input's tail
    Collection<String> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of("(game".substring(standardInput.length()));

        List<String> completions = new ArrayList<>();

        // Assuming we are starting a new ludeme
//        System.out.println(currentCompilation.longest.size() + " + " + currentCompilation.secondLongest.size());
        for (FractionalCompiler.CompilationState state: currentCompilation) {
            GenerationNode node = state.consistentGame;
            String tail = standardInput.substring(node.root().description().length());

            for (GenerationNode option: compatibleOptions(node, tail)) {
                String completion;

                if (option instanceof PrimitiveNode) {
                    completion = option.description();

                    boolean addSpace = !standardInput.endsWith(" ");
                    if (option.symbol().label != null) {
//                        System.out.println("tail:" + tail);
//                        System.out.println("label:" + option.symbol().label);

                        if (!tail.isEmpty()) {
                            if (option instanceof ContinuedPrimitive)
                                completion = completion.substring(tail.indexOf(':'));
                            else
                                completion = completion.substring(tail.length() - 1);
                        }


                        if (tail.length() > 1)
                            addSpace = false;
                    }

                    if (addSpace && !(option instanceof ContinuedPrimitive))
                        completion = " " + completion;
                }
                else {
                    String description = option.root().description();
                    if (description.length() < standardInput.length()) continue;
                    completion = description.substring(standardInput.length());
                }

                completions.add(completion);
            }
        }

        return completions;
    }

    // TODO make it consider possibilities bellow the top of the stack
    //
    public List<GenerationNode> compatibleOptions(GenerationNode node, String tail) {

//        System.out.println(node.root().description().length() + " - " + node.root().description());
//        System.out.println(node.root().description().length() + " - " + node.root());
//        System.out.println(tail);

        List<GenerationNode> completions = new ArrayList<>();

        for (GenerationNode option: node.nextPossibleParameters(symbolMap, null, false, true)) {
            assert !(option instanceof EmptyNode);

//            System.out.println("option " + (option.symbol().label == null? "":option.symbol().label) + ":" + option);

            // Verify whether the option is compatible with the tail
            if (option instanceof PrimitiveNode primitiveOption) {
                PrimitiveNode builtOption = buildPrimitive(primitiveOption, tail);
                if (builtOption != null) {
                    GenerationNode newNode = node.copyUp();
                    newNode.addParameter(builtOption);
                    completions.add(builtOption);
                }
            } else if (option instanceof EndOfClauseNode) {
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

    PrimitiveNode buildPrimitive(PrimitiveNode primitiveOption, String tail) {
        if (tail.isEmpty())
            return primitiveOption;

        String value;

        if (primitiveOption.symbol().label != null) {

            int colonIndex = tail.indexOf(':');

            // If there is no colon, the tail should be the start of a label
            if (colonIndex == -1) {
                if (!(' ' + primitiveOption.symbol().label).startsWith(tail))
                    return null;

                value = "";
            } else {
                if (!(tail.startsWith(' ' + primitiveOption.symbol().label + ':')))
                    return null;

                value = tail.substring(colonIndex + 1);
            }

        } else if (tail.charAt(0) == ' ') {
            value = tail.substring(1);
        } else {
            value = tail;
        }

        if (value.isEmpty())
            return primitiveOption;

        switch (primitiveOption.getType()) {
            case INT -> {
                if (value.equals("-")) return new ContinuedPrimitive(primitiveOption);
                try {
                    Integer.parseInt(value);
                    return new ContinuedPrimitive(primitiveOption);
                } catch (NumberFormatException ignored) {}
            }
            case FLOAT -> {
                if (value.equals("-")) return new ContinuedPrimitive(primitiveOption);
                if (value.endsWith(".")) value = value.substring(0, value.length() - 1);
                try {
                    Float.parseFloat(value);
                    return new ContinuedPrimitive(primitiveOption);
                } catch (NumberFormatException ignored) {}
            }
            case DIM -> {
                try {
                    if (Integer.parseInt(value) >= 0)
                        return new ContinuedPrimitive(primitiveOption);
                } catch (NumberFormatException ignored) {}
            }
            case STRING -> {
//                System.out.println("string value:" + value);
                if (value.startsWith("\"") && value.substring(1).indexOf('"') == -1) // Todo accept \" if we ever need to support metadata
                    return new ContinuedPrimitive(primitiveOption);
            }
            case BOOLEAN -> {
                if ("True".startsWith(value) || "False".startsWith(value))
                    return new ContinuedPrimitive(primitiveOption);
            }
        }

        return null;
    }

    String respond(String standardInput) {
        StringBuilder sb = new StringBuilder();
        HashSet<String> completions = new HashSet<>();

        for (String completion : autocomplete(standardInput)) {
            //assert option.root().description().startsWith(standardInput);

            if (!completions.contains(completion)) {
                completions.add(completion);
                sb.append(completion).append("|").append("TODO").append("||");
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
//        if (standardInput.length() > 5)
//            previousCompilation = FractionalCompiler.compileFraction(standardInput.substring(0, standardInput.lastIndexOf(' ')), symbolMap);
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

    static class ContinuedPrimitive extends PrimitiveNode {

        public ContinuedPrimitive(PrimitiveNode primitiveNode) {
            super(primitiveNode.symbol(), primitiveNode.parent());
            setValue(primitiveNode.value());
        }

        @Override
        public String toString() {
            if (value() == null)
                return super.toString().replace("NEW_", "CONTINUED_");

            return super.toString();
        }

//        @Override
//        public String description() {
//            return super.description().replace("NEW_", "CONTINUED_");
//        }
    }
}
