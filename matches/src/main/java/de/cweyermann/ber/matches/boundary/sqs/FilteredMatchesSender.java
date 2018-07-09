package de.cweyermann.ber.matches.boundary.sqs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch.Status;
import de.cweyermann.ber.matches.boundary.persistence.Repository;
import de.cweyermann.ber.matches.entity.Match;
import io.vavr.Tuple2;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class FilteredMatchesSender {

    private static final Status DONE = Status.RATED;

    @Autowired
    protected Repository repo;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @SqsListener(value = "NewMatches", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("RateMatches")
    public List<Match> filter(@Payload String matchesString, @Headers Map<String, Object> headers)
            throws JsonParseException, JsonMappingException, IOException {
        Match[] matchArray = mapper.readValue(matchesString, Match[].class);
        try {

            log.info("Processing {} matches", matchArray.length);

            io.vavr.collection.List<Match> vavr = io.vavr.collection.List
                    .ofAll(Arrays.asList(matchArray));

            List<String> ids = vavr.map(m -> m.getMatchId()).asJava();

            // amazon list does not support List Iterator => breaks vavr
            List<DynamoDbMatch> findByMatchIdIn = new ArrayList<>(repo.findByMatchIdIn(ids));

            Map<String, DynamoDbMatch> id2ObjectNotDone = io.vavr.collection.List
                    .ofAll(findByMatchIdIn)
                    .foldLeft(new HashMap<String, DynamoDbMatch>(), this::addToMap);

            List<Match> res = vavr.map(m -> m.getMatchId()) // [1 ,2]
                    .map(id -> id2ObjectNotDone.get(id)) // [db1, db2]
                    .zip(vavr) // [(db1, t1), (db2, t2)]
                    .filter(x -> (x == null || db(x) == null || dbStatus(x) != DONE)) // [(db1, t1)]
                    .map(tuple -> tuple._2) // [t1]
                    .asJava();

            log.info("Done... New Matches: " + res.size());
            return res;
        } catch (ProvisionedThroughputExceededException e) {
            retry(matchArray, headers);
            return Collections.emptyList();
        }

    }

    private void retry(Object payload, Map<String, Object> headers) {
        log.warn("Throughput exceeded! Waiting some time (1-60) Seconds and then try again!");
        int waitingTime = new Random(System.nanoTime()).nextInt(59) + 1;

        try {
            Thread.sleep(waitingTime * 1000);
        } catch (InterruptedException e1) {
            // if u insist...
        }

        String retryCount = (String) headers.get("retryCount");
        if (retryCount == null) {
            retryCount = "1";
        } else {
            retryCount = (Integer.parseInt(retryCount) + 1) + "";
        }
        headers.put("retryCount", retryCount);

        if (Integer.parseInt(retryCount) < 5) {
            queueMessagingTemplate.convertAndSend("NewMatches", payload, headers);
        } else {
            headers.put("reason", "ProvisionedThroughputExceededException");
            queueMessagingTemplate.convertAndSend("DeadLetters", payload, headers);
        }
    }

    private HashMap<String, DynamoDbMatch> addToMap(HashMap<String, DynamoDbMatch> old,
            DynamoDbMatch newO) {
        old.put(newO.getId(), newO);
        return old;
    }

    private Status dbStatus(Tuple2<DynamoDbMatch, Match> tuple) {
        DynamoDbMatch db = db(tuple);
        return db.getProcessStatus();
    }

    private DynamoDbMatch db(Tuple2<DynamoDbMatch, Match> tuple) {
        return tuple._1();
    }
}
