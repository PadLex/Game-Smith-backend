package approaches.symbolic.api;

import approaches.symbolic.CachedMapper;
import approaches.symbolic.SymbolMapper;
import approaches.symbolic.nodes.GameNode;
import supplementary.experiments.eval.EvalGames;

import java.util.Scanner;

import static approaches.symbolic.PartialCompiler.*;

/*
    * Used by the extension to evaluate up to what point a game description compiles.
    * It does not yet support definitions. But it can tell you where the error is.
    *
    * @author Alexander Padula
 */
public class PartialCompile {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Ready");

        SymbolMapper symbolMapper = new CachedMapper();

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            input = input.replace("\\n", "\n");
            PartialCompilation partialCompilation = compilePartialDescription(standardize(input), symbolMapper);
            GameNode gameNode = partialCompilation.consistentGames.peek().consistentGame.root();
            String compilingPortion = gameNode.description();
            boolean compiles = partialCompilation.exception == null;
            System.out.println(compiles? 1:0);
            System.out.println(compiles? EvalGames.defaultEvaluationFast(gameNode.compile()):0);
            System.out.println(destandardize(input, compilingPortion).replace("\n", "\\n"));
        }
        sc.close();
    }
}
