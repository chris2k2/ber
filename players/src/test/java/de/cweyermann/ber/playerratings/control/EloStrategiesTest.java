package de.cweyermann.ber.playerratings.control;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.cweyermann.ber.playerratings.control.EloStrategies;
import de.cweyermann.ber.playerratings.control.Elo.ResultStrategy;
import de.cweyermann.ber.playerratings.entity.Result;

public class EloStrategiesTest {

    @Test
    public void simpleWinLoose()
    {
        ResultStrategy res = EloStrategies.SIMPLE_WIN_LOOSE;

        assertEquals(1.0, res.getScoreModifier(Result.HOME_2_SET), 0.01);
        assertEquals(1.0, res.getScoreModifier(Result.HOME_3_SET), 0.01);
        assertEquals(0.0, res.getScoreModifier(Result.AWAY_2_SET), 0.01);
        assertEquals(0.0, res.getScoreModifier(Result.AWAY_2_SET), 0.01);
    }
}
