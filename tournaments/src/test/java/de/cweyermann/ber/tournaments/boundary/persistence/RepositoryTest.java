package de.cweyermann.ber.tournaments.boundary.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.tournaments.LocalDynamoConfig;
import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.ProccessingStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LocalDynamoConfig.class })
public class RepositoryTest {

    @Autowired
    private Repository repo;

    @Before
    public void clearRepo() {
        repo.deleteAll();
    }

    @Test
    public void integrationTest_running() {
        assertNotNull(repo);
    }

    @Test
    public void save_findAll() throws ParseException {
        repo.save(tournament("11", "20180201", "asdf", ProccessingStatus.DONE));
        repo.save(tournament("31", "20180201", "asdf", ProccessingStatus.DONE));

        assertEquals(2, repo.findAll().size());
    }

    @Test
    public void findAllByStatus() throws ParseException {
        repo.save(tournament("1", "20180201", "asdf", ProccessingStatus.DONE));
        repo.save(tournament("2", "20180201", "asdf", ProccessingStatus.DOING));

        assertEquals(1, repo.findByStatusAndSource(ProccessingStatus.DONE, "asdf").size());
    }

    @Test
    public void findAllByDate() throws ParseException {
        repo.save(tournament("1", "20180201", "asdf", ProccessingStatus.DONE));
        repo.save(tournament("2", "20180204", "asdf", ProccessingStatus.DONE));

        Date endDate = repo
                .findFirstBySourceAndStatusOrderByEndDateDesc("asdf", ProccessingStatus.DONE)
                .get()
                .getEndDate();

        assertEquals("20180204", FORMAT.format(endDate));
    }

    @Test
    public void findById() throws ParseException {
        DynamoDbTournament tournament = tournament("1", "20180201", "asdf", ProccessingStatus.DONE);
        repo.save(tournament);
        DynamoDbTournament.TournamentId id = new DynamoDbTournament.TournamentId();
        id.setId("1");
        id.setEndDate(null);
        
        assertTrue(repo.findById(tournament.getId(), tournament.getEndDate()).isPresent());
    }

    @Test
    public void findAllSources() throws ParseException {
        repo.save(tournament("1", "20180201", "1", ProccessingStatus.DONE));
        repo.save(tournament("2", "20180201", "1", ProccessingStatus.DONE));
        repo.save(tournament("3", "20180201", "1", ProccessingStatus.DONE));
        repo.save(tournament("4", "20180201", "1", ProccessingStatus.DONE));
        repo.save(tournament("5", "20180204", "2", ProccessingStatus.DONE));

        List<String> sources = repo.findDistinctBySource();

        assertEquals(Arrays.asList("1", "2"), sources);
    }

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

    public static DynamoDbTournament tournament(String id, String endDate, String source,
            ProccessingStatus status) throws ParseException {
        DynamoDbTournament t1 = new DynamoDbTournament();
        t1.setId(id);
        t1.setEndDate(FORMAT.parse(endDate));
        t1.setStatus(status);
        t1.setSource(source);

        return t1;
    }
}