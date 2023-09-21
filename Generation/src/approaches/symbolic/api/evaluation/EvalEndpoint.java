package approaches.symbolic.api.evaluation;

import approaches.symbolic.api.Endpoint;
import compiler.Compiler;
import game.Game;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import metrics.Evaluation;
import metrics.multiple.metrics.BranchingFactor;
import metrics.single.duration.DurationTurns;
import metrics.single.outcome.Balance;
import metrics.single.outcome.Completion;
import metrics.single.outcome.Drawishness;
import supplementary.experiments.eval.EvalGames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EvalEndpoint extends Endpoint {
    public static void main(String[] args) {
        new EvalEndpoint().start();
    }

    @Override
    public String respond() {
        Game game = null;
        try {
            game = (Game) Compiler.compile(new Description(rawInput), new UserSelections(new ArrayList<>()), new Report(), false);
        } catch (Exception ignored) {}

        if (game == null)
            return "-1";

        Evaluation eval = new Evaluation();
        Report report = new Report();
//        EvalGames.evaluateGame(eval, report, game, List.of(), "Random", 10, 0.1, 1000, List.of(new Balance(), new Completion()), new ArrayList<>(List.of(1d, 1d)), false);

        if (report.isError())
            return "-2";

        double[] results = EvalGames.getEvaluationScores(game, List.of(new Balance(), new Completion(), new Drawishness(), new DurationTurns()), new ArrayList<>(List.of(1d, 1d, 1d, 1d)), "Random", 100, 0.1, 1000, false, true, report);
        return String.join("|", Arrays.stream(results).mapToObj(String::valueOf).toList());
    }
}
