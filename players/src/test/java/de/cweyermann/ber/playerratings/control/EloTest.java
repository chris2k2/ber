package de.cweyermann.ber.playerratings.control;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.cweyermann.ber.playerratings.control.Elo;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Result;

public class EloTest {

    @Test
    public void sameRatingP1Wins_HigherRating21()
    {
        Elo elo = new Elo(x -> 1, (x,y) -> 1000, x -> 8, null);
        
        int calc = elo.calcDifference(1000, 1000, Result.HOME_2_SET, new Match());
        assertTrue(calc > 0);
    }
    
}
