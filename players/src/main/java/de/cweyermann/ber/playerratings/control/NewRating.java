package de.cweyermann.ber.playerratings.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;

@Component
public class NewRating {

    @Autowired
    protected DetectSex detectSex;

    @Autowired
    protected Repository repo;

    @Autowired
    protected GuessDiscipline guessDiscipline;

    public void update(Match m) {
        Discipline discipline = guessDiscipline.fromMatch(m);

        List<Match.Player> all = new ArrayList<>(m.getHomePlayers());
        all.addAll(m.getAwayPlayers());

        for (Match.Player matchPlayer : all) {
            Optional<Player> odbPlayer = repo.findById(matchPlayer.getId());
            if (!odbPlayer.isPresent()) {
                Player newPlayer = new Player();
                newPlayer.setId(matchPlayer.getId());
                newPlayer.setName(matchPlayer.getName());
                newPlayer.setSex(detectSex.fromMatch(matchPlayer.getId(), m));

                odbPlayer = Optional.of(newPlayer);
            }
            Player dbPlayer = odbPlayer.get();

            Integer rating = matchPlayer.getNewRating();
            if (discipline != null) {
                switch (discipline) {
                case WD:
                case MD:
                    dbPlayer.setRatingDoubles(rating);
                    break;
                case WS:
                case MS:
                    dbPlayer.setRatingSingles(rating);
                    break;
                case MX:
                    dbPlayer.setRatingMixed(rating);
                    break;
                default:
                    break;
                }

                repo.save(dbPlayer);
            }
        }
    }
}
