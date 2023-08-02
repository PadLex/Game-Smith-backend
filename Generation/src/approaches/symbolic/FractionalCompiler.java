package approaches.symbolic;
import approaches.symbolic.nodes.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
    * Parses a description of a game into a tree of GeneratorNodes.
    * TODO Definition support
    * TODO Support too many consistent games
    *
    * @author Alexander Padula
 */
public class FractionalCompiler {
    static final Pattern endOfParameter = Pattern.compile("[ )}]");

    public static class InternalException extends Exception {
        public InternalException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class MissmatchException extends InternalException {
        public MissmatchException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class CompilationException extends InternalException {
        public CompilationException(String errorMessage) {
            super(errorMessage);
        }
    }

    public static class CompilationState {
        public final GenerationNode consistentGame;
        public final List<GenerationNode> remainingOptions;
        public final List<InternalException> exceptions;

        public CompilationState(GenerationNode consistentGame, List<GenerationNode> remainingOptions) {
            this.consistentGame = consistentGame;
            this.remainingOptions = remainingOptions;
            this.exceptions = new ArrayList<>();
        }

        public CompilationState(GenerationNode consistentGame, List<GenerationNode> remainingOptions, List<InternalException> exceptions) {
            this.consistentGame = consistentGame;
            this.remainingOptions = remainingOptions;
            this.exceptions = exceptions;
        }
    }

    /*
     * Compiles a description of a game into a tree of GeneratorNodes.
     * @param standardInput The standardized description of the game
     * @param symbolMapper The SymbolMapper to use
     * @return The root of the tree of GeneratorNodes. Crashes if it encounters an exception
     */
    public static GameNode compileComplete(String standardInput, SymbolMap symbolMap) {
        List<CompilationState> partialCompilation = compileFraction(standardInput, symbolMap);
        if (!partialCompilation.get(0).exceptions.isEmpty()) {
            partialCompilation.get(0).exceptions.forEach(Throwable::printStackTrace); // TODO display most important errors
            System.out.println(partialCompilation.get(0).consistentGame.root().description());
            throw new RuntimeException("Error compiling " + standardInput);
        }

        if (partialCompilation.size() != 1)
            throw new RuntimeException("Failed to compile:" + standardInput);


        return partialCompilation.get(0).consistentGame.root();
    }

    /*
        * Compiles a description of a game into a tree of GeneratorNodes.
        * @param standardInput The standardized description of the game
        * @param symbolMapper The SymbolMapper to use
        * @return A PartialCompilation containing the consistent games and any exceptions that occurred
     */
    public static List<CompilationState> compileFraction(String standardInput, SymbolMap symbolMap) {
        return compileFraction(standardInput, List.of(new CompilationState(new GameNode(), List.of())), symbolMap);
    }

    /*
     * Compiles a description of a game into a tree of GeneratorNodes, starting from a tree that has already been
     * partially compiled.
     * @param standardInput The standardized description of the game
     * @param consistentGames The stack of consistent games to start from
     * @param symbolMapper The SymbolMapper to use
     * @return A PartialCompilation containing the consistent games and any exceptions that occurred
     */
    public static List<CompilationState> compileFraction(String standardInput, List<CompilationState> previouslyExcluded, SymbolMap symbolMap) {

        // The stack of all the consistent games found so far
        Stack<CompilationState> currentStack = new Stack<>();
        for (int i = previouslyExcluded.size() - 1; i >= 0; i--) {
            List<GenerationNode> nextOptions = previouslyExcluded.get(i).consistentGame.nextPossibleParameters(symbolMap, null, true, false);
            currentStack.add(new CompilationState(previouslyExcluded.get(i).consistentGame, nextOptions));
        }


//(game "Tablan" (players 2) (equipment {(board (rectangle 4 12) {(track "Track1" "0,E,N1,W,N1,E,N1,W" P1 directed:True) (track "Track2" "47,W,S1,E,S1,W,S1,E" P2 directed:True)} use:Vertex) (piece "Stick" Each (if (and (not (is In (from) (sites Next "Home"))) (!= 0 (mapEntry "Throw" (count Pips)))) (or (if (not (= 1 (var))) (move (from (from) if:(if (=
        // If a complete game isn't found, the longest consistent games are returned
        List<CompilationState> excludedStates = new Stack<>();

        // Loop until a game
        while (true) {

            // If the stack is empty, all paths lead to dead ends
            if (currentStack.isEmpty()) {
                int longestLength = 0;
                int secondLongestLength = 0;
                for (CompilationState state: excludedStates) {
                    int length = state.consistentGame.root().description().length();
                    if (length > longestLength) {
                        secondLongestLength = longestLength;
                        longestLength = length;
                    } else if (length > secondLongestLength) {
                        secondLongestLength = length;
                    }
                }
//                System.out.println("first" + longestLength);
//                System.out.println("second" + secondLongestLength);
                List<CompilationState> filteredExcluded = new ArrayList<>();
                for (CompilationState state: excludedStates) {
                    state.consistentGame.stripTrailingEmptyNodes();
                    if (state.consistentGame.root().description().length() >= secondLongestLength)
                        filteredExcluded.add(state);
                }

                filteredExcluded.sort(Comparator.comparingInt(s -> -s.consistentGame.root().description().length()));

                return filteredExcluded;
            }

            // Since we are performing a depth-first search, we can just pop the most recent game, option pair
            CompilationState state = currentStack.pop();
//            System.out.println("Considering:" + state.consistentGame.root() + " + " + state.remainingOptions.get(0));

            // If we haven't reached a dead end, remember to consider the next option
            if (state.remainingOptions.size() > 1)
                currentStack.add(new CompilationState(state.consistentGame, state.remainingOptions.subList(1, state.remainingOptions.size()), state.exceptions));

            // Loops through all options and adds them to the stack if they are consistent with the standardInput description
            try {
                GenerationNode newNode = appendOption(state.consistentGame, state.remainingOptions.get(0), standardInput);

                assert !newNode.isComplete() || newNode instanceof GameNode;
                List<GenerationNode> nextOptions = newNode.nextPossibleParameters(symbolMap, null, true, false);
                CompilationState newCompilationState = new CompilationState(newNode, nextOptions);
                currentStack.add(newCompilationState);

                // Successful termination condition
                if (newNode instanceof GameNode && newNode.isComplete()) {
                    Stack<CompilationState> out = new Stack<>();
                    out.add(new CompilationState(newNode, List.of()));
                    return out;
                }

            } catch (InternalException e) {

                if (state.remainingOptions.size() == 1)
                    excludedStates.add(state);

                state.exceptions.add(e);
            }
        }
    }

    /*
     * Appends an option to a game, if it is consistent with the standardInput description.
     * @param node The game to append to
     * @param option The option to append
     * @param standardInput The standardized description
     * @return The new game, or null if the option is not consistent with the standardInput description
     */
    static GenerationNode appendOption(GenerationNode node, GenerationNode option, String standardInput) throws InternalException {
        String currentDescription = node.root().description();

        if (currentDescription.length() >= standardInput.length())
            throw new MissmatchException("Node's description is longer then the input's");

        String trailingDescription = standardInput.substring(currentDescription.length()).strip();

        // Parse primitive options
        if (option instanceof PrimitiveNode primitiveOption) {

            // Manually deal with possible labels
            if (option.symbol().label != null) {
                String prefix = option.symbol().label + ":";
                if (!trailingDescription.startsWith(prefix))
                    throw new MissmatchException("Missing primitive label");
                trailingDescription = trailingDescription.substring(prefix.length()).strip();
            }

            switch (primitiveOption.getType()) {
                case STRING -> {
                    if (trailingDescription.isEmpty() || trailingDescription.charAt(0) != '"')
                        throw new MissmatchException("Missing the opening quote");

                    int end = trailingDescription.indexOf('"', 1);
                    if (end == -1)
                        throw new MissmatchException("Missing the terminal quote");

                    primitiveOption.setValue(trailingDescription.substring(1, end));
                }
                case INT, DIM, FLOAT -> {
                    Matcher match = endOfParameter.matcher(trailingDescription);

                    int end = trailingDescription.length();
                    if (match.find())
                        end = match.start();
//                        throw new MissmatchException("Can't find closing end of parameter");

                    try {
                        primitiveOption.setUnparsedValue(trailingDescription.substring(0, end));
                    } catch (NumberFormatException e) {
                        throw new MissmatchException("Not a number");
                    }
                }
                case BOOLEAN -> { // TODO maybe check if after the True/False there is a space or bracket
                    if (trailingDescription.startsWith("True")) {
                        primitiveOption.setUnparsedValue("True");
                    } else if (trailingDescription.startsWith("False")) {
                        primitiveOption.setUnparsedValue("False");
                    } else {
                        throw new MissmatchException("Not a boolean");
                    }
                }
            }

            GenerationNode newNode = node.copyUp();
            option.setParent(newNode);
            newNode.addParameter(option);

//            System.out.println(newNode.root().description());
            assert standardInput.startsWith(newNode.root().description());
            return newNode;
        }

        // Parse non-primitive options
        else {
            // option.description accounts for the label already
            if (!(option instanceof EmptyNode) && !(option instanceof EndOfClauseNode)) {

//                System.out.println("trailingDescription:" + trailingDescription);
//                System.out.println("option:" + option.description());
                if (!trailingDescription.startsWith(option.description()))
                    throw new MissmatchException("Wrong class " + option.description());

                if (trailingDescription.length() > option.description().length()) {
                    char nextChar = trailingDescription.charAt(option.description().length());
                    char currentChar = trailingDescription.charAt(option.description().length() - 1);
                    boolean isEnd = nextChar == ' ' || nextChar == ')' || nextChar == '}' || nextChar == '(' || nextChar == '{' || currentChar == '(' || currentChar == '{';

                    if (!isEnd)
                        throw new MissmatchException("Wrong class 2"); // TODO why not use end of parameter like before?
                }

            }

            if (option instanceof EndOfClauseNode && !trailingDescription.isEmpty()) {
                char currentChar = trailingDescription.charAt(0);
                if (currentChar != ')' && currentChar != '}')
                    throw new MissmatchException("Not a closing bracket"); // TODO I could probably handle this with the trailingDescription
            }

            GenerationNode nodeCopy = node.copyUp();
            option.setParent(nodeCopy);
            nodeCopy.addParameter(option);

            if (!standardInput.startsWith(nodeCopy.root().description())) // If slow, remove for complete games
                throw new MissmatchException("Now node does not match the input"); // TODO CHECK

            if (!option.isComplete())
                return option;

            if (nodeCopy.isComplete()) {
                assert nodeCopy.isRecursivelyComplete();

                if (nodeCopy instanceof GameNode)
                    return nodeCopy;

                try {
                    nodeCopy.compile();
                } catch (Exception e) {
                    throw new CompilationException(e.getMessage());
                }

                return nodeCopy.parent();
            }

            return nodeCopy;
        }
    }

    /*
     * Standardizes a description such that any description that is equivalent to another description has the same
     * standardized form. This is done by removing all unnecessary whitespace, replacing constants with their values,
     * and standardizing numeric values.
     * @param str The description to standardize
     * @return The standardized description
     */
    public static String standardize(String str) {
        str = str.replaceAll("\\s+", " ");
        str = str.replace("( ", "(");
        str = str.replace(" )", ")");
        str = str.replace("{ ", "{");
        str = str.replace(" }", "}");
        str = str.replaceAll("\\s:\\s", ":"); // (forEach of : (... -> (forEach of:(...
        str = str.replaceAll("(?<![\\d])\\.(\\d)", "0.$1"); // .5 -> 0.5    //TODO this is not correct 1 is not 1.0
        str = str.replaceAll("(\\d+\\.\\d*?)0+\\b", "$1"); // 0.50 -> 0.5
        str = str.replaceAll("(\\d)+\\.([^0-9])", "$1$2"); // 0. -> 0

        // Constants
        str = str.replaceAll("([ ({])Off([ )}])", "$1-1$2");
        str = str.replaceAll("([ ({])End([ )}])", "$1-2$2");
        str = str.replaceAll("([ ({])Undefined([ )}])", "$1-1$2");

        return str;
    }

    /*
     * Undes the standardization of a description. Given the compilable portion of the description in standard form,
     *  it recovers the compilable portion of the description in its original form.
     * It does this py converting the standard form into a regular expression and then using that regular expression to
     *  match the equivalent section in the original description.
     * @param original The original description
     * @param standard A standardized substring of the description
     */
    public static String destandardize(String original, String standard) {
        String pattern = standard;
        pattern = pattern.replace(" ", "\\s+");
        pattern = pattern.replace("(", "\\(\\s*");
        pattern = pattern.replace(")", "\\s*\\)");
        pattern = pattern.replace("{", "\\{\\s*");
        pattern = pattern.replace("}", "\\s*\\}");
        pattern = pattern.replace(":", "\\s*:\\s*");
        pattern = pattern.replaceAll("\\d+\\.?\\d*", "\\\\d+\\\\.?\\\\d*");
        pattern = pattern.replaceAll("[ ({]-1[ )}]", "[ ({](Off)|(-1)[ )}]");
        pattern = pattern.replaceAll("[ ({]-2[ )}]", "[ ({](End)|(-2)[ )}]");
        pattern = pattern.replaceAll("[ ({]-1[ )}]", "[ ({](Undefined)|(-1)[ )}]");

        Matcher matcher = Pattern.compile(pattern).matcher(original);

        if (matcher.lookingAt())
            return original.substring(0, matcher.end());

        return "";
    }

}
