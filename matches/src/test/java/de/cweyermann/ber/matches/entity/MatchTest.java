package de.cweyermann.ber.matches.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.modelmapper.ModelMapper;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;

public class MatchTest {

    @Test
    public void modelMapper_canUseGetter()
    {
        ModelMapper mapper = new ModelMapper();
        
        Match m = new Match();
        
        String hash1 = m.getId();
        String matchId1 = mapper.map(m, DynamoDbMatch.class).getId();
        
        m.setResult("something");

        String matchId2 = mapper.map(m, DynamoDbMatch.class).getId();
        String hash2 = m.getId();
        
        assertNotEquals(matchId1, matchId2);
        assertEquals(hash1, matchId1);
        assertEquals(hash2, matchId2);
    }
}
