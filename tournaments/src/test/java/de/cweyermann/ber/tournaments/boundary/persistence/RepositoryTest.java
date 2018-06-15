package de.cweyermann.ber.tournaments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.tournaments.boundary.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.Repository;

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

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
    
    public static DynmoDbTournament tournament(String id, String endDate, String source,
            ProccessingStatus status) throws ParseException {
        DynmoDbTournament t1 = new DynmoDbTournament();
        t1.setId(id);
        t1.setEndDate(FORMAT.parse(endDate));
        t1.setStatus(status);
        t1.setSource(source);

        return t1;
    }
}