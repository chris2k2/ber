package de.cweyermann.ber.players.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.players.entity.DetectSex;
import de.cweyermann.ber.players.entity.Match;
import de.cweyermann.ber.players.entity.Match.Discipline;
import de.cweyermann.ber.players.entity.Player;

@Component
public class UpdatePlayerRating {

    @Autowired
    protected DetectSex detectSex;

    @Autowired
    protected Repository repo;

    @SqsListener(value = "UpdatePlayerRating", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void command(@Payload List<Match> matches) {
        for (Match m : matches) {
            Discipline discipline = m.getDiscipline();

            List<Match.Player> all = new ArrayList<>(m.getHomePlayers());
            all.addAll(m.getAwayPlayers());

            for (Match.Player matchPlayer : all) {
                Optional<Player> odbPlayer = repo.findById(matchPlayer.getId());
                if (!odbPlayer.isPresent()) {
                    Player newPlayer = new Player();
                    newPlayer.setId(matchPlayer.getId());
                    newPlayer.setName(matchPlayer.getName());
                    newPlayer.setSex(detectSex.fromMatches(matchPlayer.getId(), matches));

                    odbPlayer = Optional.of(newPlayer);
                }
                Player dbPlayer = odbPlayer.get();

                Integer rating = matchPlayer.getNewRating();
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
