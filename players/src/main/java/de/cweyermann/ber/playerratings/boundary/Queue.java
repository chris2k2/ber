package de.cweyermann.ber.playerratings.boundary;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.playerratings.control.RatingFrontend;
import de.cweyermann.ber.playerratings.entity.Match;

@Component
public class Queue {

    @Autowired
    protected RatingFrontend rating;

    @SqsListener(value = "RateMatches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("RatedMatches")
    public List<Match> rateNewMatches(@Payload List<Match> matches) {
        matches.stream().forEach(m -> rating.singleMatch(m));

        return matches;
    }
}
