package de.cweyermann.ber.playerratings.control;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;
import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Player.Sex;

@Service
public class GuessDiscipline {

    @Autowired
    protected Repository repo;

    public Discipline fromPlayersCombination(List<Player> players) {
        int males = 0;
        int females = 0;

        for (Player player : players) {
            Sex sex = player.getSex();
            if (sex == Sex.M) {
                males++;
            } else if (sex == Sex.F) {
                females++;
            }
        }

        if (males > 2) {
            return Discipline.MD;
        } else if (females > 2) {
            return Discipline.WD;
        } else if (players.size() == 4) {
            return Discipline.MX;
        } else if (males > 0) {
            return Discipline.MS;
        } else if (females > 0) {
            return Discipline.WS;
        }

        return null;
    }

    public Discipline fromMatch(Match m) {
        Discipline result = m.getDiscipline();
        if (result == null) {
            List<Match.Player> all2 = new ArrayList<>(m.getHomePlayers());
            all2.addAll(m.getAwayPlayers());

            List<Player> players = all2.stream()
                    .filter(p -> p.getId() != null)
                    .map(p -> repo.findById(p.getId()))
                    .filter(p -> p.isPresent())
                    .map(p -> p.get())
                    .collect(Collectors.toList());

            result = fromPlayersCombination(players);
            m.setDiscipline(result);
        }
        return result;
    }
}
