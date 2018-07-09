package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;

import de.cweyermann.ber.tournaments.boundary.sqs.CrawlCommandScheduler.CrawlError;
import de.cweyermann.ber.tournaments.control.Crawl;
import de.cweyermann.ber.tournaments.entity.CrawlCommand;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CrawlCommandScheduler {

    @Data
    public class CrawlError {
        String stacktrace;

        String reason;

        Date time;
    }

    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Autowired
    private Crawl crawl;

    @Scheduled(fixedRate = 500000)
    public void reportCurrentTime() {
        try {
            List<CrawlCommand> commands = crawl.getCommands();

            for (CrawlCommand c : commands) {
                log.info("Start Crawling: " + c);
                queueMessagingTemplate.convertAndSend("Crawl", c);
            }
        } catch (Exception e) {
            CrawlError error = new CrawlError();
            error.stacktrace = ExceptionUtils.getStackTrace(e);
            error.reason = e.getMessage();
            error.time = new Date();

            queueMessagingTemplate.convertAndSend("DeadLetters", error);
            log.error("Problems at Crawling: " + error);
        }
    }
}