package approaches.symbolic.api;

import approaches.symbolic.FractionalCompiler;
import approaches.symbolic.nodes.GameNode;
import supplementary.experiments.eval.EvalGames;

import java.util.List;
import java.util.Stack;

import static approaches.symbolic.FractionalCompiler.*;

public class ThoroughEvaluationEndpoint extends CachedEndpoint {
    public static void main(String[] args) {
        EvalGames.debug = false;
        new ThoroughEvaluationEndpoint().start();
    }

    @Override
    String respond() {
        List<CompilationState> partialCompilation = compileFraction(standardize(rawInput), symbolMap);
        boolean compiles = partialCompilation.get(0).exceptions.isEmpty();
        if (!compiles)
            return "";

        GameNode gameNode = partialCompilation.get(0).consistentGame.root();
        return String.valueOf(EvalGames.defaultEvaluationSlow(gameNode.compile()));
    }
}
