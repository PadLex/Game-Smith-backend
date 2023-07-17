
## Evaluation Metrics

The main methods in the updated evaluation system can be found in EvalGames.java, which can be located in `supplementary/experiments/eval/EvalGames.java`.  I have also added a new metric named Systematicity, which can be found in `metrics/designer/Systematicity.java`.  The main methods that I have added are as follow (they are all commented, for further insight examine said comments):

* getEvaluationScores - Simply evaluates a game.  Provides multiple input and output options, so simplifies internal use of the evaluation function.

* defaultEvaluationFast - Quickly evaluates a game with some tested settings.

* defaultEvaluationSlow - Evaluates a game with some tested settings more thoroughly.

* loadDB - Loads two pre-built CSV files into memory for later use.

* recommendScore - Recommends an evaluation score for a game based its' nearest neighbors - either BoardGameGeek scores or metric scores can be used to do this.  For this method, loadDB is necessary.
