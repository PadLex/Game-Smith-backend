package metrics.designer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.UnixPrintWriter;
import main.math.LinearRegression;
import metrics.Evaluation;
import metrics.Metric;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.mcts.MCTS;
import utils.AIFactory;

public class Interaction extends Metric
{
    public Interaction()
    {
        super(
                "Interaction",
                "Measure of random-proofness in a game",
                0.0,
                1.0,
                null
        );
    }

    @Override
    public Double apply
            (
                    Game game,
                    Evaluation evaluation,
                    Trial[] trials,
                    RandomProviderState[] randomProviderStates
            )
    {
        final List<Double> strongAIRanking = new ArrayList<>();

        final List<AI> ais = new ArrayList<AI>(game.players().count() + 1);
        int smartPlayerIndex = 1;
        ais.add(null);
        ais.add(MCTS.createUCT());

        for (int p = 2; p <= game.players().count(); ++p)
            ais.add(AIFactory.createAI("Random"));

        // TODO play games and examine ranking of UCT player, the worse it is the less interaction there is



        return null;
    }
}
