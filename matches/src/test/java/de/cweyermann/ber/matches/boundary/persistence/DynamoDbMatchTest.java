package de.cweyermann.ber.matches.boundary.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class DynamoDbMatchTest {

    @Test
    public void homeAwayNotNull_playerIdsFilled()
    {
        DynamoDbMatch match = new DynamoDbMatch();
        DynamoDbPlayer player1 = new DynamoDbPlayer();
        player1.setId("1");
        DynamoDbPlayer player2 = new DynamoDbPlayer();
        player2.setId("2");
        DynamoDbPlayer player3 = new DynamoDbPlayer();
        player3.setId("3");
        DynamoDbPlayer player4 = new DynamoDbPlayer();
        player4.setId("4");
        match.setHomePlayers(Arrays.asList(player1, player2));
        match.setAwayPlayers(Arrays.asList(player3, player4));
        
        match.fillPlayers();

        assertEquals("1", match.getPlayer1Id());
        assertEquals("2", match.getPlayer2Id());
        assertEquals("3", match.getPlayer3Id());
        assertEquals("4", match.getPlayer4Id());
    }
    
    @Test
    public void homeAwaySinglesNotNull_playerIdsFilled()
    {
        DynamoDbMatch match = new DynamoDbMatch();
        DynamoDbPlayer player1 = new DynamoDbPlayer();
        player1.setId("1");
        DynamoDbPlayer player3 = new DynamoDbPlayer();
        player3.setId("3");
        match.setHomePlayers(Arrays.asList(player1 ));
        match.setAwayPlayers(Arrays.asList(player3));
        
        match.fillPlayers();

        assertEquals("1", match.getPlayer1Id());
        assertEquals("3", match.getPlayer2Id());
        assertEquals(null, match.getPlayer3Id());
        assertEquals(null, match.getPlayer4Id());
    }
    
    @Test
    public void strangeMatch_nothingFilled()
    {
        DynamoDbMatch match = new DynamoDbMatch();
        DynamoDbPlayer player1 = new DynamoDbPlayer();
        player1.setId("1");
        DynamoDbPlayer player3 = new DynamoDbPlayer();
        player3.setId("3");
        match.setHomePlayers(Arrays.asList(player1 ));
        match.setAwayPlayers(Arrays.asList(player3, player3));
        
        match.fillPlayers();

        assertEquals(null, match.getPlayer1Id());
        assertEquals(null, match.getPlayer2Id());
        assertEquals(null, match.getPlayer3Id());
        assertEquals(null, match.getPlayer4Id());
    }
}
