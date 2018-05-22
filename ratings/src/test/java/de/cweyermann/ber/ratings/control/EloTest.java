package de.cweyermann.ber.ratings.control;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Result;

public class EloTest {

    
    @Test
    public void sameRatingP1Wins_P1HigherRating()
    {
        Elo elo = new Elo(x -> 1, (x,y) -> 1000, x -> 8);
        
        int calc = elo.calcDifference(1000, 1000, Result.HOME_2_SET, new Match());
        assertTrue(calc > 0);
    }
}
