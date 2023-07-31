package approaches.symbolic.nodes;

import approaches.symbolic.SymbolMap;
import approaches.symbolic.SymbolMap.MappedSymbol;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Node representing an enum ludeme. This is a terminal node.
 */
public class EnumNode extends GenerationNode {
    EnumNode(MappedSymbol symbol, GenerationNode parent) {
        super(symbol, parent);
    }

    Object instantiate() {
        try {
            return symbol.cls().getMethod("valueOf", String.class).invoke(null, symbol.name());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to compile enum node: " + e);
        }
    }

    public List<GenerationNode> nextPossibleParameters(SymbolMap symbolMap) {
        return List.of();
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void addParameter(GenerationNode param) {
        throw new RuntimeException("Enum nodes are terminal");
    }

    @Override
    public String toString() {
        return symbol.grammarLabel();
    }
}
