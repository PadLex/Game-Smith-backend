package approaches.symbolic.nodes;

import approaches.symbolic.SymbolMap;

import java.util.List;

/**
 * Node representing an unused argument. This is a terminal node.
 */
public class EmptyNode extends GenerationNode {
    public EmptyNode(GenerationNode parent) {
        super(SymbolMap.emptySymbol, parent);
    }

    @Override
    Object instantiate() {
        return null;
    }

    @Override
    public List<GenerationNode> nextPossibleParameters(SymbolMap symbolMap) {
        return List.of();
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @Override
    public String buildDescription() {
        throw new RuntimeException("Empty nodes don't have a description");
    }
}
