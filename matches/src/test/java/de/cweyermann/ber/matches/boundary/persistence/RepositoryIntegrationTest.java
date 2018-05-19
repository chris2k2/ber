package de.cweyermann.ber.matches.boundary.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    public void findByPlayer1Id()
    {
        DynamoDbMatch entity = new DynamoDbMatch();
        entity.setId("1");
        entity.setPlayer1Id("player1id");
        repository.save(entity);
        
        assertEquals(entity, repository.findByPlayer1Id("player1id").get(0));
    }
    
    @Test
    public void findByAnyPlayerId()
    {
        DynamoDbMatch entity = new DynamoDbMatch();
        entity.setId("1");
        entity.setPlayer1Id("ich");
        repository.save(entity);
        
        DynamoDbMatch entity2 = new DynamoDbMatch();
        entity2.setId("2");
        entity2.setPlayer1Id("BLA");
        repository.save(entity2);
        
        DynamoDbMatch entity3 = new DynamoDbMatch();
        entity3.setId("3");
        entity3.setPlayer1Id("ich");
        repository.save(entity3);
        
        assertEquals(2, repository.findByAnyPlayerId("ich").size());
    }

}