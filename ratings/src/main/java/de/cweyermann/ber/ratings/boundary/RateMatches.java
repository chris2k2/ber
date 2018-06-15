package de.cweyermann.ber.ratings.boundary;

import static de.cweyermann.ber.ratings.control.EloStrategies.AVERAGE;
import static de.cweyermann.ber.ratings.control.EloStrategies.EVERYONE_1000;
import static de.cweyermann.ber.ratings.control.EloStrategies.K_CONST8;
import static de.cweyermann.ber.ratings.control.EloStrategies.SIMPLE_WIN_LOOSE;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.ratings.control.Elo;
import de.cweyermann.ber.ratings.control.Rating;
import de.cweyermann.ber.ratings.entity.Match;

@Component
public class RateMatches {

    @Autowired
    protected QueueMessagingTemplate queue;

    private static final Elo OPTIMAL = new Elo(SIMPLE_WIN_LOOSE, EVERYONE_1000, K_CONST8, AVERAGE);

    @SqsListener(value = "RateMatches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("RatedMatches")
    public List<Match> execute(List<Match> matches) {
        Rating rating = new Rating(OPTIMAL);

        rating.updateUnratedMatches(matches);

        queue.convertAndSend("UpdatePlayerRating", matches);
        return matches;
    }
}
