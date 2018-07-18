package de.cweyermann.ber.tournaments.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.CrawlCommand;

public class CrawlTest {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
    
    private Repository repo;
    private Crawl crawl;

    @Before
    public void initRepo() {
        repo = mock(Repository.class);
        crawl = new Crawl(repo);
    }

    @Test
    public void nothing_nothing() {
        assertTrue(crawl.getCommands().isEmpty());
    }

    @Test
    public void threeTournaments2Sources_maxDateEach() throws ParseException
    {
        DynamoDbTournament t1 = new DynamoDbTournament();
        t1.setId("1");
        t1.setEndDate(FORMAT.parse("20180101"));
        t1.setSource("source1");

        DynamoDbTournament t2 = new DynamoDbTournament();
        t2.setId("2");
        t2.setEndDate(FORMAT.parse("20180201"));
        t2.setSource("source1");
        
        DynamoDbTournament t3 = new DynamoDbTournament();
        t3.setId("3");
        t3.setEndDate(FORMAT.parse("20180115"));
        t3.setSource("source2");

        when(repo.findFirstBySourceOrderByEndDateDesc("source1")).thenReturn(Optional.of(t2));
        when(repo.findFirstBySourceOrderByEndDateDesc("source2")).thenReturn(Optional.of(t3));
        when(repo.findDistinctBySource()).thenReturn(Arrays.asList("source1", "source2"));

        List<CrawlCommand> commands = crawl.getCommands();

        assertEquals("2018-02-01", commands.get(0).getNiceLastEndDate());
        assertEquals("source1", commands.get(0).getSource());
        assertEquals("2018-01-15", commands.get(1).getNiceLastEndDate());
        assertEquals("source2", commands.get(1).getSource());
    }
}
