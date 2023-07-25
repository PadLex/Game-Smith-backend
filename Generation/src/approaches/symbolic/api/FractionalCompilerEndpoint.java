package approaches.symbolic.api;

import approaches.symbolic.nodes.GameNode;
import supplementary.experiments.eval.EvalGames;

import java.util.Stack;

import static approaches.symbolic.FractionalCompiler.*;

/*
    * Used by the extension to evaluate up to what point a game description compiles.
    * It does not yet support definitions. But it can tell you where the error is.
    *
    * @author Alexander Padula
 */
public class FractionalCompilerEndpoint extends CachedEndpoint {

    public static void main(String[] args) {
        new FractionalCompilerEndpoint().start();
    }

    @Override
    String respond() {
        Stack<CompilationState> partialCompilation = compileFraction(standardize(rawInput), symbolMapper);
        GameNode gameNode = partialCompilation.peek().consistentGame.root();
        String compilingPortion = gameNode.description();
        boolean compiles = partialCompilation.peek().exceptions.isEmpty();
        return (compiles ? "1|" + EvalGames.defaultEvaluationFast(gameNode.compile()) : "0|0") +
                "|" + destandardize(rawInput, compilingPortion);
    }
}
