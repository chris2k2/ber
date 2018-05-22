package de.cweyermann.ber.players.boundary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.cweyermann.ber.players.entity.Player;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { LocalDynamoConfig.class })
public class RepositoryIntegrationTest {

    @Autowired
    private Repository repository;

    @Test
    public void integrationTest_running() {
        assertNotNull(repository);
    }
    
    @Test
    public void cansave_twice() {
        Player p = new Player();
        p.setId("asdf");
        p.setName("alt");

        repository.save(p);
        
        p = new Player();
        p.setId("asdf");
        p.setName("neu");
        repository.save(p);

        assertEquals(1, repository.count());
        assertEquals("neu", repository.findById("asdf").get().getName());
    }
    
}