package approaches.symbolic.api;

import approaches.symbolic.CachedMap;
import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.SymbolMap;
import approaches.symbolic.nodes.*;

import java.util.*;

import static approaches.symbolic.FractionalCompiler.standardize;


public class AutocompleteEndpoint extends CachedEndpoint {

    // Compiles the game as far as it goes and returns all completions that start with the input's tail
    Collection<String> autocomplete(String standardInput) {
        if ("(gam".startsWith(standardInput))
            return List.of("(game".substring(standardInput.length()));

        List<String> completions = new ArrayList<>();

        // Assuming we are starting a new ludeme
//        System.out.println(currentCompilation.longest.size() + " + " + currentCompilation.secondLongest.size());
        for (FractionalCompiler.CompilationState state: cachedCompilation) {
            GenerationNode node = state.consistentGame;
            String tail = standardInput.substring(node.root().description().length());

            for (GenerationNode option: compatibleOptions(node, tail)) {
                if (option instanceof PrimitiveNode) {
                    String completion = option.description();

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

                    completions.add(completion);
                } else if (option instanceof EndOfClauseNode) {
                    if (node.root().description().length() + 1 < standardInput.length()) continue;
                    completions.addAll(consecutiveClosingBrackets(node));
                } else if (option instanceof ArrayNode) {
                    String description = option.root().description();
                    if (description.length() < standardInput.length()) continue;

                    String label = description.substring(standardInput.length());
//                    System.out.println(label + ", " + option.symbol().nesting());

                    completions.add(label + "{".repeat(option.symbol().nesting()-1));
                } else {
                    String description = option.root().description();
                    if (description.length() < standardInput.length()) continue;

                    completions.add(description.substring(standardInput.length()));
                }
            }
        }

        return completions;
    }

    public List<String> consecutiveClosingBrackets(GenerationNode node) {
        node = node.copyUp();

        List<String> brackets = new ArrayList<>(List.of(""));
        int depth = 0;
        while (node != null) {
            EndOfClauseNode endOfClauseNode = null;
            boolean onlyOption = true;
            for (GenerationNode option: node.nextPossibleParameters(symbolMap, null, false, true)) {
                if (option instanceof EndOfClauseNode endNode) {
                    endOfClauseNode = endNode;
                    break;
                } else {
                    onlyOption = false;
                }
            }

            if (endOfClauseNode == null)
                break;

            char bracket = node instanceof ArrayNode? '}' : ')';

            List<String> newBrackets = new ArrayList<>();

            for (String old: brackets) {
                if (old.length() == depth)
                    newBrackets.add(old + bracket);

                if (old.length() != depth || !onlyOption && depth>0)
                    newBrackets.add(old);
            }

            // Update outer variables
            brackets = newBrackets;
            node.addParameter(endOfClauseNode);
            node = node.parent();
            depth++;
        }

        return brackets;
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

    @Override
    String respond() {
        StringBuilder sb = new StringBuilder();
        HashSet<String> completions = new HashSet<>();
        String standardInput = standardize(rawInput);

        for (String completion : autocomplete(standardInput)) {
            //assert option.root().description().startsWith(standardInput);

            if (!completions.contains(completion) && !completion.isEmpty()) {
                completions.add(completion);
                sb.append(completion).append("|").append("TODO").append("||");
            }
        }

        if (sb.length() > 2)
            sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
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

        @Override
        public String description() {
            return super.description().replace("NEW_", "CONTINUED_");
        }
    }
}
