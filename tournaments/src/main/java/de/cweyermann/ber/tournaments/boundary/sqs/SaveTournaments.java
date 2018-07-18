package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.TournamentId;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SaveTournaments {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    protected Repository repo;

    @Autowired
    private ModelMapper mapper;

    @SqsListener(value = "NewTournaments", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void sendUnprocessed(@Payload List<Tournament> tournaments) {

        try {
            log.info("Saving tournaments: {}", tournaments.size());
            List<DynamoDbTournament> tournamentsInDb = new ArrayList<>();
            for (Tournament tournament : tournaments) {
                tournamentsInDb.add(mapper.map(tournament, DynamoDbTournament.class));
            }

            List<String> ids = tournamentsInDb.stream().map(x -> x.getId()).collect(
                    Collectors.toList());
            List<String> doneIds = repo.findByIdIn(ids)
                    .stream()
                    .filter(t -> t.getStatus() != ProccessingStatus.UNPROCESSED)
                    .map(t -> t.getId())
                    .collect(Collectors.toList());

            List<DynamoDbTournament> notDoneTs = tournamentsInDb.stream().filter(t -> !doneIds.contains(t.getId())).collect(Collectors.toList());
            
            repo.saveAll(notDoneTs);
            log.info("Saved tournaments: {}", notDoneTs.size());
        } catch (Exception e) {
            log.error("Problems at Crawling: " + e);
            throw e;
        }
    }
}
