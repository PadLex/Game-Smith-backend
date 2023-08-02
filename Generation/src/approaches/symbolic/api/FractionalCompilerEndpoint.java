package approaches.symbolic.api;

import approaches.symbolic.nodes.GameNode;
import supplementary.experiments.eval.EvalGames;

import java.util.List;

import static approaches.symbolic.FractionalCompiler.*;

/*
    * Used by the extension to evaluate up to what point a game description compiles.
    * It does not yet support definitions. But it can tell you where the error is.
    *
    * @author Alexander Padula
 */
public class FractionalCompilerEndpoint extends CachedEndpoint {

    public static void main(String[] args) {
        EvalGames.debug = false;
        new FractionalCompilerEndpoint().start();
    }

    @Override
    String respond() {
        CompilationCheckpoint partialCompilation = compileFraction(standardize(rawInput), symbolMap);
//        partialCompilation.forEach(s -> System.out.println(s.consistentGame.root().description()));
        GameNode gameNode = partialCompilation.longest.get(0).consistentGame.root();
        String compilingPortion = gameNode.description();
        boolean compiles = partialCompilation.longest.get(0).exceptions.isEmpty();
        return (compiles ? ("1|" + EvalGames.defaultEvaluationFast(gameNode.compile())) : "0|0") +
                "|" + destandardize(rawInput, compilingPortion);
    }
}
