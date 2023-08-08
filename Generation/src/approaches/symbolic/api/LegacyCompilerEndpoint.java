package approaches.symbolic.api;
import java.io.OutputStream;
import java.io.PrintStream;

import compiler.Compiler;
import game.Game;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import supplementary.experiments.eval.EvalGames;

import java.util.ArrayList;


/*
 * Used by the extension to evaluate whether a game description compiles.
 * It supports definitions but can't tell you which part of the description is causing the error.
 *
 * @author Alexander Padula
 */
public class LegacyCompilerEndpoint extends Endpoint {

    public static void main(String[] args) {
        new LegacyCompilerEndpoint().start();
    }

    @Override
    String respond() {
        Game game = null;
        try {
            game = (Game) Compiler.compile(new Description(rawInput), new UserSelections(new ArrayList<>()), new Report(), false);
        } catch (Exception ignored) {}

        return game != null? "1|" + EvalGames.defaultEvaluationFast(game) : "0|0";
    }
}
