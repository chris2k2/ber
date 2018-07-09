package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.sqs.CrawlCommandScheduler.CrawlError;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ScrapCommandSender {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    protected Repository repo;

    @Scheduled(fixedRate = 5000)
    public void sendUnprocessed() {

        try {
            List<DynmoDbTournament> list = new ArrayList<>();
            list.addAll(repo.findByStatus(ProccessingStatus.UNPROCESSED));
            list.addAll(repo.findByStatus(ProccessingStatus.ONGOING));

            queueMessagingTemplate.convertAndSend("Scrap", list);
            log.info("send scrap to Queue");
        } catch (Exception e) {
            QueueError error = new QueueError();
            error.stacktrace = ExceptionUtils.getStackTrace(e);
            error.reason = e.getMessage();
            error.time = new Date();

            queueMessagingTemplate.convertAndSend("DeadLetters", error);
            log.error("Problems at Crawling: " + error);
        }
    }
}
