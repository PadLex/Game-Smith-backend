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
        new FractionalCompilerEndpoint().start();
    }

    @Override
    String cachedResponse() {
        CompilationCheckpoint partialCompilation = compileFraction(standardInput, symbolMap);
//        partialCompilation.longest.forEach(s -> System.out.println(s.consistentGame.root().nodeCount() + ": " + s.consistentGame.root().description()));
//        partialCompilation.secondLongest.forEach(s -> System.out.println(s.consistentGame.root().nodeCount() + ": " + s.consistentGame.root().description()));

//        partialCompilation.forEach(s -> System.out.println(s.consistentGame.root().description()));
        GameNode gameNode = partialCompilation.longest.get(0).consistentGame.root();
        String compilingPortion = gameNode.description();
        boolean compiles = partialCompilation.longest.get(0).exceptions.isEmpty() && gameNode.isRecursivelyComplete();
//        System.out.println("Compiles:" + compiles);
        double eval = 0;
        try {
            if (compiles)
                eval = EvalGames.defaultEvaluationFast(gameNode.compile());
        } catch (Exception internalError) {
            eval = 0;
            internalError.printStackTrace();
        }

        return (compiles ? "1":"0") + '|' + eval + "|" + destandardize(rawInput, compilingPortion);
    }
}
