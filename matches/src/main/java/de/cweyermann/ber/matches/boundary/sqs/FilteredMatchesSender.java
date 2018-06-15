package de.cweyermann.ber.matches.boundary.sqs;

import java.io.IOException;
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

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch.Status;
import de.cweyermann.ber.matches.boundary.persistence.Repository;
import de.cweyermann.ber.matches.entity.Match;
import io.vavr.Tuple2;

@Component
public class FilteredMatchesSender {

    private static final Status DONE = Status.RATED;

    @Autowired
    protected Repository repo;

    @Autowired
    protected ObjectMapper mapper;
    
    @SqsListener(value = "NewMatches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("AddOldRating2Matches")
    public List<Match> filter(@Payload String matchesString) throws JsonParseException, JsonMappingException, IOException {
        Match[] matchArray = mapper.readValue(matchesString, Match[].class);
        
        io.vavr.collection.List<Match> vavr = io.vavr.collection.List.ofAll(Arrays.asList(matchArray));

        return vavr.map(m -> m.getMatchId()) // [1 ,2]
                .map(id -> repo.findByMatchId(id)) // [db1, db2]
                .zip(vavr) // [(db1, t1), (db2, t2)]
                .filter(x -> (!db(x).isPresent() || dbStatus(x) != DONE)) // [(db1, t1)]
                .map(tuple -> tuple._2) // [t1]
                .asJava();
    }

    private Status dbStatus(Tuple2<Optional<DynamoDbMatch>, Match> tuple) {
        return db(tuple).get().getProcessStatus();
    }

    private Optional<DynamoDbMatch> db(Tuple2<Optional<DynamoDbMatch>, Match> tuple) {
        return tuple._1();
    }
}
