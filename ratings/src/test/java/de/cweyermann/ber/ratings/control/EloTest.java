package de.cweyermann.ber.ratings.control;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import de.cweyermann.ber.ratings.control.Elo.Result;
import de.cweyermann.ber.ratings.entity.Match;

public class EloTest {

    
    @Test
    public void sameRatingP1Wins_P1HigherRating()
    {
        Elo elo = new Elo(x -> 1, (x,y) -> 1000, x -> 8);
        
        Pair<Integer, Integer> calc = elo.calc(1000, 1000, Result.HOME_2_SET, new Match());
        
        int rating1 = calc.getLeft();
        int rating2 = calc.getRight();
        
        assertTrue(rating1 > rating2);
        assertTrue(rating1 + rating2 == 2000);
    }
}
