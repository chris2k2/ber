package de.cweyermann.ber.playerratings.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResultTest {

    @Test
    public void twoSetHomeWins() {
        assertResult(Result.HOME_2_SET, "21:19 21:10");
    }

    @Test
    public void threeSetHomeWins() {
        assertResult(Result.HOME_3_SET, "10:19 21:10 21:13");
    }

    @Test
    public void twoSetAwayWins() {
        assertResult(Result.AWAY_2_SET, "21:23 21:23");
    }

    @Test
    public void threeSetAwayWins() {
        assertResult(Result.AWAY_3_SET, "21:19 21:23 29:30");
    }

    @Test
    public void worksWithDash() {
        assertResult(Result.AWAY_3_SET, "21-19 21:23 29:30");
    }

    @Test
    public void moreSets_definedAsWonNothing() {
        assertResult(Result.HOME_2_SET, "11:9 11:8 12:10");
        assertResult(Result.AWAY_3_SET, "11:8 0:11 0:11 0:11");
    }
    
    private void assertResult(Result expected, String score) {
        Result result = Result.fromResultString(score);
        assertEquals(expected, result);
    }
}
