package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ScrapCommandFilter {

    private static final int MINUTES = 1000 * 60;

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    protected Repository repo;

    @Autowired
    private ModelMapper mapper;

    // monday-friday at 2:00:00 am
    // @Scheduled(cron = "0 0 2 * * 1-5")
    @Scheduled(fixedRate = 10 * MINUTES)
    public void sendUnprocessed() {

        try {

            List<DynamoDbTournament> list = new ArrayList<>();
            list.addAll(repo.findByStatus(ProccessingStatus.UNPROCESSED));
//            list.addAll(repo.findByStatus(ProccessingStatus.ONGOING));

            List<Tournament> tournamentsOut = new ArrayList<>();
            for (DynamoDbTournament tournament : list) {
                tournamentsOut.add(mapper.map(tournament, Tournament.class));
            }

            log.info("Processing {} tournaments", list.size());
            if (list.size() > 10) {
                queueMessagingTemplate.convertAndSend("Scrap", list.subList(0, 10));
            }
            log.info("Sent to Scrap");
        } catch (Exception e) {
            QueueError error = new QueueError();
            error.stacktrace = ExceptionUtils.getStackTrace(e);
            error.reason = e.getMessage();
            error.time = new Date();
            error.component = getClass().getName();

            queueMessagingTemplate.convertAndSend("DeadLetters", error);
            log.error("Problems at Scrapping: " + error);
        }
    }
}
