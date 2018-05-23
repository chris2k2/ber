package de.cweyermann.ber.ratings.control;

import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Result;
import lombok.extern.log4j.Log4j2;

/**
 * This class implements the business logic regarding ELO rankings. It uses the
 * strategy pattern to update all relevant fields
 * 
 * @author chris
 *
 */
@Log4j2
public class Elo {

    private ResultStrategy result;
    private InitStrategy init;
    private KStrategy kStragey;
    private DoublesStrategy doublesStrategy;

    @FunctionalInterface
    public interface ResultStrategy {
        double getScoreModifier(Result result);
    }

    @FunctionalInterface
    public interface InitStrategy {
        int getInitialScoreByMatch(Match match, Match.Player player);
    }

    @FunctionalInterface
    public interface KStrategy {
        int getKFactor(Match match);
    }

    @FunctionalInterface
    public interface DoublesStrategy {
        int getEloRatingForDoubles(int rating1, int rating2);
    }

    public Elo(ResultStrategy result, InitStrategy init, KStrategy kStragey,
            DoublesStrategy doublesStrategy) {
        this.result = result;
        this.init = init;
        this.kStragey = kStragey;
        this.doublesStrategy = doublesStrategy;
    }

    public int init(Match match, Match.Player player) {
        return init.getInitialScoreByMatch(match, player);
    }

    public int calcDifference(int player1Rating, int player2Rating, Result outcome, Match match) {
        double actualScore = result.getScoreModifier(outcome);
        int k = kStragey.getKFactor(match);

        // calculate expected outcome
        int newRating1 = calcRating(player1Rating, player2Rating, k, actualScore);
        int diff = newRating1 - player1Rating;

        log.info("New Rating k={}: [{}, {}] -> [{}, {}]", k, player1Rating, player2Rating,
                newRating1, player2Rating - diff);

        return diff;
    }

    public int calcDoublesDifference(int player1Rating, int player2Rating, int player3rating,
            int player4rating, Result outcome, Match match) {

        int team1rating = doublesStrategy.getEloRatingForDoubles(player1Rating, player2Rating);
        int team2rating = doublesStrategy.getEloRatingForDoubles(player3rating, player4rating);

        return calcDifference(team1rating, team2rating, outcome, match);
    }

    private int calcRating(int player1Rating, int player2Rating, int k, double actualScore) {
        double exponent = (double) (player2Rating - player1Rating) / 400;
        double expectedOutcome = (1 / (1 + (Math.pow(10, exponent))));

        // calculate new rating
        return (int) Math.round(player1Rating + k * (actualScore - expectedOutcome));
    }
}