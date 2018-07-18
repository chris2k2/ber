package de.cweyermann.ber.matches.boundary.sqs;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch.Status;
import de.cweyermann.ber.matches.boundary.persistence.Repository;
import de.cweyermann.ber.matches.entity.Match;

@Component
public class PersistRankedMatches {
    @Autowired
    private ModelMapper mapper;
    
    @Autowired
    protected Repository repo;

    @SqsListener(value = "RatedMatches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void persist(List<Match> matches) {
        
        List<DynamoDbMatch> dynamoMatches = matches.stream()
                .map(m -> mapper.map(m, DynamoDbMatch.class))
                .collect(Collectors.toList());
        dynamoMatches.forEach(m -> m.update());
        dynamoMatches.forEach(m -> m.setProcessStatus(Status.RATED));

        repo.saveAll(dynamoMatches);
    }
}
