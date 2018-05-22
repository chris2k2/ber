package de.cweyermann.ber.boundary;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.ratings.boundary.Repository;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Match.Status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LocalDynamoConfig.class })
public class RepositoryIntegrationTest {

    @Autowired
    private Repository repo;

    @Test
    public void integrationTest_running() {
        assertNotNull(repo);
    }
    
    @Test
    public void saveRegularMatch_CanBeFetched()
    {
        Match m = new Match();
        m.setLeague("any League");
        m.setProcessStatus(Status.UNRATED);
        
        Match m2 = new Match();
        m2.setLeague("any League");
        m2.setProcessStatus(Status.RATED);
        
        repo.saveAll(Arrays.asList(m, m2));
        
        List<Match> list = repo.getAllUnratedMatches();
        
        assertEquals(1, list.size());
    }

}