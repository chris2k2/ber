package de.cweyermann.ber.playerratings.control;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Result;
import de.cweyermann.ber.playerratings.entity.Match.Player;

/**
 * This is the rating Service. It uses {@link Elo} to get the actual rating. And
 * then it updates the business objects.
 * 
 * @author chris
 *
 */
@Service
public class RatingFrontend {

    @Autowired
    protected Elo eloAlgo;

    @Autowired
    protected OldRating oldRating;

    @Autowired
    protected NewRating newRating;

    public void singleMatch(Match match) {
        Result res = Result.fromResultString(match.getResult());

        oldRating.addIfKnown(match);

        calcNewRatings(match, res, Player::getOldRating, Player::setNewRating);

        newRating.update(match);
    }

    public void calcNewRatings(Match match, Result res, Function<Player, Integer> getRating,
            BiConsumer<Player, Integer> setRating) {
        List<Player> homePlayers = match.getHomePlayers();
        List<Player> awayPlayers = match.getAwayPlayers();

        init(match, homePlayers);
        init(match, awayPlayers);

        List<Integer> homeRatings = homePlayers.stream().map(getRating).collect(
                Collectors.toList());

        List<Integer> awayRatings = awayPlayers.stream().map(getRating).collect(toList());

        int diff = calcDifference(match, res, homeRatings, awayRatings);

        for (int i = 0; i < homeRatings.size(); i++) {
            Player homeDbPlayer = homePlayers.get(i);
            Match.Player homeMatchPlayer = match.getHomePlayers().get(i);
            int oldHomeRating = homeRatings.get(i);

            update(setRating, homeDbPlayer, homeMatchPlayer, oldHomeRating, oldHomeRating + diff);

            Player awayDbPlayer = awayPlayers.get(i);
            Match.Player awayMatchPlayer = match.getAwayPlayers().get(i);
            int oldAwayRating = awayRatings.get(i);

            update(setRating, awayDbPlayer, awayMatchPlayer, oldAwayRating, oldAwayRating - diff);
        }
    }

    private void init(Match match, List<Player> players) {
        for (Match.Player p : players) {
            if (p.getOldRating() == null) {
                p.setOldRating(eloAlgo.init(match, p));
            }
        }
    }

    private int calcDifference(Match match, Result res, List<Integer> homeRatings,
            List<Integer> awayRatings) {
        int diff = 0;
        if (homeRatings.size() == 1) {
            diff = eloAlgo.calcDifference(homeRatings.get(0), awayRatings.get(0), res, match);
        } else if (homeRatings.size() == 2) {
            diff = eloAlgo.calcDoublesDifference(homeRatings.get(0), homeRatings.get(1),
                    awayRatings.get(0), awayRatings.get(1), res, match);
        }
        return diff;
    }

    private void update(BiConsumer<Player, Integer> setRating, Player homeDbPlayer,
            Match.Player homeMatchPlayer, int oldHomeRating, int newHomeRating) {
        homeMatchPlayer.setOldRating(oldHomeRating);
        homeMatchPlayer.setNewRating(newHomeRating);
        setRating.accept(homeDbPlayer, newHomeRating);
    }
}
