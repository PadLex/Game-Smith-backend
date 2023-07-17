package approaches.symbolic.nodes;

import approaches.symbolic.SymbolMapper;
import approaches.symbolic.SymbolMapper.MappedSymbol;
import main.StringRoutines;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a node in the generator tree. Each node tracks its symbol, parent, and parameters. Each node can tell you
 * what parameters it can take next, and can compile itself by instantiating the symbol's ludeme class.
 */
public abstract class GeneratorNode {
    final MappedSymbol symbol;
    final List<GeneratorNode> parameterSet = new ArrayList<>();
    GeneratorNode parent;
    Object compilerCache = null;
    String descriptionCache = null;
    boolean complete;

    GeneratorNode(MappedSymbol symbol, GeneratorNode parent) {
        assert symbol != null;
        this.symbol = symbol;
        this.parent = parent;
    }

    /**
     * Identifies the correct type of node to create for a given symbol.
     * @param symbol The symbol to create a node for.
     * @param parent The parent of the node.
     * @return A new node of the appropriate type for the symbol.
     */
    public static GeneratorNode fromSymbol(MappedSymbol symbol, GeneratorNode parent) {
        if (symbol.nesting() > 0) {
            return new ArrayNode(symbol, parent);
        }

        if (PrimitiveNode.typeOf(symbol.path()) != null)
            return new PrimitiveNode(symbol, parent);

        if (symbol.cls().isEnum())
            return new EnumNode(symbol, parent);

        switch (symbol.path()) {
            case "mapper.unused" -> {
                return new EmptyNode(parent);
            }
            case "mapper.endOfClause" -> {
                return new EndOfClauseNode(parent);
            }
            case "game.Game" -> {
                return new GameNode();
            }
        }

        return new ClassNode(symbol, parent);
    }

    /**
     * Recursively compiles the ludeme represented by this node. This method is memoized, so it will only compile the
     * ludeme once.
     * @return The compiled ludeme.
     */
    public Object compile() {
        if (compilerCache == null)
            compilerCache = instantiate();

        return compilerCache;
    }

    abstract Object instantiate();

    public abstract List<GeneratorNode> nextPossibleParameters(SymbolMapper symbolMapper);

    public List<GeneratorNode> nextPossibleParameters(SymbolMapper symbolMapper, List<GeneratorNode> partialArguments, boolean includeAliases, boolean expandEmpty) {
        List<GeneratorNode> options;

        if (partialArguments == null || partialArguments.isEmpty()) {
            options = nextPossibleParameters(symbolMapper);
        } else {
            int i = parameterSet.size();
            parameterSet.addAll(partialArguments);
            options = nextPossibleParameters(symbolMapper);
            parameterSet.subList(i, parameterSet.size()).clear();
        }

        if (includeAliases || expandEmpty)
            options = new ArrayList<>(options);

        // Expand empty nodes
        if (expandEmpty) {
            EmptyNode empty = options.stream().filter(n -> n instanceof EmptyNode).map(n -> (EmptyNode) n).findFirst().orElse(null);
            if (empty != null) {
                options.remove(empty);
                List<GeneratorNode> nextPartialArguments = partialArguments==null? new ArrayList<>() : new ArrayList<>(partialArguments);
                nextPartialArguments.add(empty);
                options.addAll(nextPossibleParameters(symbolMapper, nextPartialArguments, includeAliases, expandEmpty));
            }
        }

        // Add aliases to the options (^ ... should also include (pow ...
        if (includeAliases) {
            options.addAll(options.stream().filter(n -> n.symbol().hasAlias()).map(n -> {
                MappedSymbol noAlias = new MappedSymbol(n.symbol());
                noAlias.setToken(StringRoutines.toDromedaryCase(noAlias.name()));
                return GeneratorNode.fromSymbol(noAlias, n.parent());
            }).toList());
        }

        return options;
    }

    public void addParameter(GeneratorNode param) {
        assert param != null;
        param.parent = this;

        if (param instanceof EndOfClauseNode) {
            complete = true;
            return;
        }

        parameterSet.add(param);
    }

    public void popParameter() {
        parameterSet.remove(parameterSet.size() - 1);
        clearCache();
        complete = false;
    }

    public void clearParameters() {
        parameterSet.clear();
        clearCache();
        complete = false;
    }

    public void clearCache() {
        if (parent.compilerCache != null)
            parent.clearCache();

        compilerCache = null;
        descriptionCache = null;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isRecursivelyComplete() {
        return isComplete() && parameterSet.stream().allMatch(GeneratorNode::isRecursivelyComplete);
    }

    public MappedSymbol symbol() {
        return symbol;
    }

    public GeneratorNode parent() {
        return parent;
    }

    public void setParent(GeneratorNode parent) {
        this.parent = parent;
    }

    public List<GeneratorNode> parameterSet() {
        return Collections.unmodifiableList(parameterSet);
    }

    public GeneratorNode find(String token) {
        for (GeneratorNode node : parameterSet) {
            if (node.symbol.token().equals(token))
                return node;
        }

        return null;
    }

    /**
     * Copies the node and all of its grandchildren down to the leaves.
     * @return A copy of the node and all of its grandchildren.
     */
    public GeneratorNode copyDown() {
        GeneratorNode clone = fromSymbol(symbol, parent);
        clone.parameterSet.addAll(parameterSet.stream().map(GeneratorNode::copyDown).toList());
        clone.complete = complete;
        clone.compilerCache = compilerCache;
        return clone;
    }

    /**
     * Copies the node and all of its ancestors up to the root node.
     * @return A copy of the node and all of its ancestors.
     */
    public GeneratorNode copyUp() {
        GeneratorNode clone = fromSymbol(symbol, parent);
        clone.parameterSet.addAll(parameterSet);
        clone.complete = complete;
        clone.compilerCache = compilerCache;
        if (parent != null) {
            clone.parent = parent.copyUp();
            clone.parent.parameterSet.set(parent.parameterSet.indexOf(this), clone);
        }

        return clone;
    }

    /**
     * Note: this function is memoized
     * @return A representation of the game in compilable standard form as defined in DescriptionParser
     */
    public String description() {
        if (descriptionCache == null)
            descriptionCache = buildDescription();

        return descriptionCache;
    }

    String buildDescription() {
        if (symbol.label != null)
            return symbol.label + ":" + this.toString();

        return this.toString();
    }

    public GameNode root() {
        GeneratorNode node = this;
        while (node.parent != null)
            node = node.parent;

        return (GameNode) node;
    }

    public GeneratorNode get(List<Integer> indexes) {
        GeneratorNode node = this;
        for (int i : indexes) {
            node = node.parameterSet.get(i);
        }
        return node;
    }

}
