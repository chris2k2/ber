package de.cweyermann.ber.tournaments;

import static de.cweyermann.ber.tournaments.TournamentBuilder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.tournaments.boundary.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LocalDynamoConfig.class })
public class RepositoryIntegrationTest {

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

        Date endDate = repo.findFirstBySourceAndStatusOrderByEndDateDesc("asdf", ProccessingStatus.DONE)
                .get()
                .getEndDate();

        assertEquals("20180204", FORMAT.format(endDate));
    }

}