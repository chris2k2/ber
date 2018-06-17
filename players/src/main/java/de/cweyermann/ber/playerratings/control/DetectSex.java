package de.cweyermann.ber.playerratings.control;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;
import de.cweyermann.ber.playerratings.entity.Player.Sex;

@Component
public class DetectSex {

    private Repository repo;

    @Autowired
    public DetectSex(Repository repo) {
        this.repo = repo;
    }

    public Player.Sex fromMatch(String id, Match match) {
        Player.Sex sex = Sex.UNKNOWN;

        sex = guessByDiscipline(match);
        if (sex == Sex.UNKNOWN) {
            sex = guessByPartner(id, match);
        }

        return sex;
    }

    private Sex guessByPartner(String id, Match match) {
        Player.Sex sex = Sex.UNKNOWN;

        if (match.getDiscipline() == Discipline.MX) {
            Sex partnerSex = partnerSex(id, match, match.getHomePlayers());
            if (partnerSex == null) {
                partnerSex = partnerSex(id, match, match.getAwayPlayers());
            }

            sex = invert(partnerSex);
        }

        return sex;
    }

    private Player.Sex invert(Sex partnerSex) {
        Player.Sex sex = Sex.UNKNOWN;

        if (partnerSex != null) {
            if (partnerSex == Sex.M) {
                sex = Sex.F;
            } else {
                sex = Sex.M;
            }
        }
        return sex;
    }

    private Sex partnerSex(String id, Match match, List<Match.Player> homePlayers) {
        Sex partnerSex = null;
        Match.Player partner = getPartner(id, match, homePlayers);

        if (partner != null) {
            Optional<Player> partnerInDb = repo.findById(partner.getId());
            if (partnerInDb.isPresent()) {
                partnerSex = partnerInDb.get().getSex();
            }
        }
        return partnerSex;
    }

    private Match.Player getPartner(String id, Match match, List<Match.Player> players) {
        Match.Player partner = null;

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(id)) {
                partner = players.get(i ^ 1);
            }
        }
        return partner;
    }

    private Player.Sex guessByDiscipline(Match match) {
        Player.Sex sex = Sex.UNKNOWN;

        if (match.getDiscipline() == Discipline.MS || match.getDiscipline() == Discipline.MD) {
            sex = Sex.M;
        } else if (match.getDiscipline() == Discipline.WS
                || match.getDiscipline() == Discipline.WD) {
            sex = Sex.F;
        }

        return sex;
    }
}
