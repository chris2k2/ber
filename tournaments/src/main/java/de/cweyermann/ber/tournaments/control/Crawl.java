package de.cweyermann.ber.tournaments.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.CrawlCommand;

@Component
public class Crawl {

    private Repository repo;

    @Autowired
    public Crawl(Repository repo) {
        this.repo = repo;
    }

    public List<CrawlCommand> getCommands() {
        List<CrawlCommand> crawls = new ArrayList<>();

        for (String source : repo.findDistinctBySource()) {
            Optional<DynamoDbTournament> firstT = repo.findFirstBySourceOrderByEndDateDesc(source);
            if (firstT.isPresent()) {
                DynamoDbTournament tournament = firstT.get();

                CrawlCommand command = new CrawlCommand();
                command.setLastEndDate(tournament.getEndDate());
                command.setSource(source);
                crawls.add(command);
            }
        }

        return crawls;
    }
}