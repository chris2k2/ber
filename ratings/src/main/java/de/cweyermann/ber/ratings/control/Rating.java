package de.cweyermann.ber.ratings.control;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import de.cweyermann.ber.ratings.boundary.Repository;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.EmbeededMatchPlayer;
import de.cweyermann.ber.ratings.entity.Result;

public class Rating {

    private Repository repo;
    private Elo eloAlgo;

    @Autowired
    public Rating(Repository repo, Elo eloAlgo) {
        this.repo = repo;
        this.eloAlgo = eloAlgo;
    }

    public void addAll() {
        List<Match> matches = repo.getAllUnratedMatches();

        for (Match match : matches) {
            Result res = Result.fromResultString(match.getResult());

            EmbeededMatchPlayer player11 = match.getHomePlayers().get(0);
            EmbeededMatchPlayer player21 = match.getAwayPlayers().get(0);

            if (match.getHomePlayers().size() == 1) {
                int diff = eloAlgo.calcDifference(player11.getRating(), player21.getRating(), res,
                        match);

                player11.setAfterRating(player11.getRating() + diff);
                player21.setAfterRating(player21.getRating() - diff);
            } else {
                EmbeededMatchPlayer player12 = match.getHomePlayers().get(1);
                EmbeededMatchPlayer player22 = match.getAwayPlayers().get(1);

                int average1 = (player11.getRating() + player12.getRating()) / 2;
                int average2 = (player21.getRating() + player22.getRating()) / 2;

                int diff = eloAlgo.calcDifference(average1, average2, res, match);

                player11.setAfterRating(player11.getRating() + diff);
                player12.setAfterRating(player12.getRating() + diff);
                player21.setAfterRating(player21.getRating() - diff);
                player22.setAfterRating(player22.getRating() - diff);
            }

            repo.save(match);
        }
    }
}
