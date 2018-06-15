package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.control.Crawl;
import de.cweyermann.ber.tournaments.entity.CrawlCommand;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CrawlCommandScheduler {

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    private Crawl crawl;

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        List<CrawlCommand> commands = crawl.getCommands();

        for (CrawlCommand c : commands) {
            log.info("Start Crawling: " + c);
            queueMessagingTemplate.convertAndSend("Crawl", c);
        }
    }
}