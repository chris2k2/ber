package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;

@Component
public class ScrapCommandSender {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;
    
    @Autowired
    protected Repository repo;

    @Scheduled(fixedRate = 5000)
    public void sendUnprocessed() {

        List<DynmoDbTournament> list = repo.findByStatus(ProccessingStatus.UNPROCESSED);
        list.addAll(repo.findByStatus(ProccessingStatus.ONGOING));
        
        queueMessagingTemplate.convertAndSend("Scrap", list);
    }
}
