package de.cweyermann.ber.playerratings.control;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.control.Elo.DoublesStrategy;
import de.cweyermann.ber.playerratings.control.Elo.InitStrategy;
import de.cweyermann.ber.playerratings.control.Elo.KStrategy;
import de.cweyermann.ber.playerratings.control.Elo.ResultStrategy;
import de.cweyermann.ber.playerratings.control.EloStrategies.LeagueDepth;
import de.cweyermann.ber.playerratings.control.tools.InMemoryPlayerRepo;
import de.cweyermann.ber.playerratings.control.tools.LocalDynamoConfig;
import de.cweyermann.ber.playerratings.control.tools.MatchRepo;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Match.Player;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LocalDynamoConfig.class })
public class EloOptimizer {

    @Autowired
    private MatchRepo matchRepo;

    private ModelMapper mapper = new ModelMapper();

    private InMemoryPlayerRepo fakeRepo;

    @Test
    public void test() {
        List<DynamoDbMatch> matches = new ArrayList<DynamoDbMatch>();
        matchRepo.findAll().forEach(matches::add);
        List<Match> realMatches = matches.stream()
                .map(match -> mapper.map(match, Match.class))
                .collect(Collectors.toList());

        RatingFrontend frontend = build();
        ResultStrategy resultStrategy = EloStrategies.SIMPLE_WIN_LOOSE;
        InitStrategy initStrategy = EloStrategies.EVERYONE_1500;
        DoublesStrategy doublesStrategy = EloStrategies.AVERAGE;
        KStrategy kStragey = EloStrategies.K_CONST8;
        frontend.eloAlgo = new Elo(resultStrategy, initStrategy, kStragey, doublesStrategy);

        run("Simple Win/Loose -- Everyone 15000 -- Average -- const 8", frontend, realMatches);

        frontend.eloAlgo.result = new EloStrategies.WeigtendResult(0.99);
        run("ThreeSet 0.99 -- Everyone 1500 -- Average -- const 8", frontend, realMatches);

        frontend.eloAlgo.init = new EloStrategies.LeagueDepth(50, true);
        run("ThreeSet 0.99 -- LeagueDepth(50) + Fallback -- Average -- const 8", frontend, realMatches);

        double start = 0.2;
        for(int i=0 ; i <= 10 ; i++)
        {
            frontend.eloAlgo.doublesStrategy = new EloStrategies.WeightendAverageDoubles(start, 1-start);
            run("ThreeSet 0.99 -- LeagueDepth(50) + Fallback -- Stronger " + start + " -- const 8", frontend, realMatches);

            start += 0.05;
        }
        
    }

    private int run(String name, RatingFrontend frontend, List<Match> realMatches) {
        int matchesWithBeforeRating = 0;
        int upsets = 0;

        fakeRepo.clear();
        for (Match m : realMatches) {
            frontend.singleMatch(m);

            DoublesStrategy dStrategy = frontend.eloAlgo.doublesStrategy;
            Integer homeBefore = getRating(dStrategy, m.getHomePlayers());
            Integer awayBefore = getRating(dStrategy, m.getAwayPlayers());
            Integer homeAfter = getAfterRating(dStrategy, m.getHomePlayers());
            Integer awayAfter = getAfterRating(dStrategy, m.getAwayPlayers());

            if (ratingAvailable(homeBefore, awayBefore) && ratingAvailable(homeAfter, awayAfter)) {
                matchesWithBeforeRating++;

                if (homeBefore > awayBefore && homeBefore > homeAfter) {
                    upsets++;
                } else if (homeBefore < awayBefore && awayBefore > awayAfter) {
                    upsets++;
                }
            }
        }

        System.out.println("--------------------------");
        System.out.println(name);
        System.out.println("Matches with Before Rating: " + matchesWithBeforeRating);
        System.out.println("Upsets: " + upsets);

        return upsets;
    }

    private boolean ratingAvailable(Integer homeBefore, Integer awayBefore) {
        return homeBefore != null && awayBefore != null;
    }

    private Integer getRating(DoublesStrategy doublesStrategy, List<Player> players) {
        if (players.size() == 1) {
            return players.get(0).getOldRating();
        } else if (players.size() == 2) {
            return doublesStrategy.getEloRatingForDoubles(players.get(0).getOldRating(),
                    players.get(1).getOldRating());
        }

        return null;
    }

    private Integer getAfterRating(DoublesStrategy doublesStrategy, List<Player> players) {
        if (players.size() == 1 && players.get(0) != null) {
            return players.get(0).getNewRating();
        } else if (players.size() == 2 && players.get(0) != null && players.get(1) != null
                && players.get(0).getNewRating() != null && players.get(1).getNewRating() != null) {
            return doublesStrategy.getEloRatingForDoubles(players.get(0).getNewRating(),
                    players.get(1).getNewRating());
        }
        return null;
    }

    private RatingFrontend build() {
        fakeRepo = new InMemoryPlayerRepo();

        RatingFrontend frontend = new RatingFrontend();
        frontend.newRating = new NewRating();
        frontend.newRating.detectSex = new DetectSex();
        frontend.newRating.detectSex.repo = fakeRepo;
        frontend.newRating.guessDiscipline = new GuessDiscipline();
        frontend.newRating.guessDiscipline.repo = fakeRepo;
        frontend.newRating.repo = fakeRepo;
        frontend.oldRating = new OldRating();
        frontend.oldRating.guessDiscipline = frontend.newRating.guessDiscipline;
        frontend.oldRating.repo = fakeRepo;
        return frontend;
    }

}
