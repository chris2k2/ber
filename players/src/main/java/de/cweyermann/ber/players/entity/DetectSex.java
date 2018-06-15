package de.cweyermann.ber.players.entity;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.players.boundary.Repository;
import de.cweyermann.ber.players.entity.Match.Discipline;
import de.cweyermann.ber.players.entity.Player.Sex;

@Component
public class DetectSex {

    private Repository repo;

    @Autowired
    public DetectSex(Repository repo) {
        this.repo = repo;
    }

    public Player.Sex fromMatches(String id, List<Match> matches) {
        Player.Sex sex = Sex.UNKNOWN;

        sex = guessByDiscipline(matches);
        if (sex == Sex.UNKNOWN) {
            sex = guessByPartner(id, matches);
        }

        return sex;
    }

    private Sex guessByPartner(String id, List<Match> matches) {
        Player.Sex sex = Sex.UNKNOWN;

        for (Match match : matches) {
            if (match.getDiscipline() == Discipline.MX) {
                Sex partnerSex = partnerSex(id, match, match.getHomePlayers());
                if (partnerSex == null) {
                    partnerSex = partnerSex(id, match, match.getAwayPlayers());
                }

                sex = invert(partnerSex);
            }
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

    private Player.Sex guessByDiscipline(List<Match> matches) {
        Player.Sex sex = Sex.UNKNOWN;

        for (Match match : matches) {
            if (match.getDiscipline() == Discipline.MS || match.getDiscipline() == Discipline.MD) {
                sex = Sex.M;
            } else if (match.getDiscipline() == Discipline.WS
                    || match.getDiscipline() == Discipline.WD) {
                sex = Sex.F;
            }
        }
        return sex;
    }
}
