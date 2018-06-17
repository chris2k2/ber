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
public class OldRating {

    @Autowired
    protected Repository repo;

    public void addIfKnown(Match m) {
        Discipline discipline = m.getDiscipline();

        List<Match.Player> all = new ArrayList<>(m.getHomePlayers());
        all.addAll(m.getAwayPlayers());

        for (Match.Player matchPlayer : all) {
            Optional<Player> odbPlayer = repo.findById(matchPlayer.getId());
            if (odbPlayer.isPresent()) {
                Player dbPlayer = odbPlayer.get();

                Integer rating = null;
                switch (discipline) {
                case WD:
                case MD:
                    rating = dbPlayer.getRatingDoubles();
                    break;
                case WS:
                case MS:
                    rating = dbPlayer.getRatingSingles();
                    break;
                case MX:
                    rating = dbPlayer.getRatingMixed();
                    break;
                default:
                    break;
                }

                matchPlayer.setOldRating(rating);
            }
        }
    }
}
