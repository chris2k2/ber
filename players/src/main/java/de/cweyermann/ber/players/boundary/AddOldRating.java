package de.cweyermann.ber.players.boundary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cweyermann.ber.players.entity.Match;
import de.cweyermann.ber.players.entity.Match.Discipline;
import de.cweyermann.ber.players.entity.Player;

@Component
public class AddOldRating {

    @Autowired
    protected Repository repo;

    @Autowired
    protected ObjectMapper mapper;

    @SqsListener(value = "AddOldRating2Matches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("RateMatches")
    public List<Match> ifThereAdd(@Payload String matchesString) throws JsonParseException, JsonMappingException, IOException {
        
        List<Match> matches = Arrays.asList(mapper.readValue(matchesString, Match[].class));
        for (Match m : matches) {
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

        return matches;
    }
}
