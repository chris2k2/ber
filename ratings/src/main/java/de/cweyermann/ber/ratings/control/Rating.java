package de.cweyermann.ber.ratings.control;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import de.cweyermann.ber.ratings.boundary.PlayersClient;
import de.cweyermann.ber.ratings.boundary.Repository;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Match.Status;
import de.cweyermann.ber.ratings.entity.Player;
import de.cweyermann.ber.ratings.entity.Result;
import io.vavr.Tuple2;

public class Rating {

    private Repository repo;
    private Elo eloAlgo;
    private PlayersClient client;

    @Autowired
    public Rating(Repository repo, Elo eloAlgo, PlayersClient client) {
        this.repo = repo;
        this.eloAlgo = eloAlgo;
        this.client = client;
    }

    public void updateUnratedMatches() {
        List<Match> matches = repo.getAllUnratedMatches();

        for (Match match : matches) {
            Result res = Result.fromResultString(match.getResult());

            // it's a Singles match
            List<Player> players = new ArrayList<>();
            if (match.getDiscipline().endsWith("S")) {
                players = calcNewRatings(match, res, Player::getRatingSingles, Player::setRatingSingles);
            } else if (match.getDiscipline().endsWith("D")) {
                players = calcNewRatings(match, res, Player::getRatingDoubles, Player::setRatingDoubles);
            } else if (match.getDiscipline().equals("MX")) {
                players = calcNewRatings(match, res, Player::getRatingMixed, Player::setRatingMixed);
            }

            players.forEach(p -> client.update(p.getId(), p));
            match.setProcessStatus(Status.RATED);
            repo.save(match);
        }
    }

    private List<Player> calcNewRatings(Match match, Result res, Function<Player, Integer> getRating,
            BiConsumer<Player, Integer> setRating) {
        List<Player> homePlayers = match.getHomePlayers()
                .stream()
                .map(mp -> client.getSingle(mp.getId()))
                .collect(toList());

        List<Player> awayPlayers = match.getAwayPlayers()
                .stream()
                .map(mp -> client.getSingle(mp.getId()))
                .collect(toList());

        init(match, homePlayers, match.getHomePlayers());
        init(match, awayPlayers, match.getAwayPlayers());
        
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
            int oldAwayRating = homeRatings.get(i);

            update(setRating, awayDbPlayer, awayMatchPlayer, oldAwayRating, oldAwayRating - diff);
        }
        
        return combineList(homePlayers, awayPlayers);
    }

    private void init(Match match, List<Player> players, List<Match.Player> matchPlayers) {
        for(int i=0; i < matchPlayers.size(); i++)
        {
            Match.Player matchPlayer = matchPlayers.get(i);
            
            if(players.get(i) == null)
            {
                Player p = map(match, matchPlayer);
                players.set(i, p);
            }
        }
    }

    private Player map(Match match, Match.Player matchPlayer) {
        Player p = new Player();
        p.setId(matchPlayer.getId());
        p.setName(matchPlayer.getName());
        p.setRatingSingles(eloAlgo.init(match, matchPlayer));
        p.setRatingDoubles(eloAlgo.init(match, matchPlayer));
        p.setRatingMixed(eloAlgo.init(match, matchPlayer));
        client.update(p.getId(), p);
        return p;
    }

    private List<Player> combineList(List<Player> homePlayers, List<Player> awayPlayers) {
        List<Player> all = new ArrayList<>();
        all.addAll(homePlayers);
        all.addAll(awayPlayers);
        return all;
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
        homeMatchPlayer.setRating(oldHomeRating);
        homeMatchPlayer.setAfterRating(newHomeRating);
        setRating.accept(homeDbPlayer, newHomeRating);

    }
}
