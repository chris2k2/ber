package de.cweyermann.ber.ratings.control;

import de.cweyermann.ber.ratings.control.Elo.DoublesStrategy;
import de.cweyermann.ber.ratings.control.Elo.InitStrategy;
import de.cweyermann.ber.ratings.control.Elo.KStrategy;
import de.cweyermann.ber.ratings.control.Elo.ResultStrategy;
import de.cweyermann.ber.ratings.entity.Result;

public class EloStrategies {

    public static final ResultStrategy SIMPLE_WIN_LOOSE = EloStrategies::simpleWinLoose;

    public static final InitStrategy EVERYONE_1000 = (x, y) -> 1000;

    public static final KStrategy K_CONST8 = x -> 8;

    public static final DoublesStrategy AVERAGE = (r1, r2) -> (r1 + r2) / 2;

    private static double simpleWinLoose(Result result) {
        double ret = 0;

        if (result == Result.HOME_2_SET || result == Result.HOME_3_SET) {
            ret = 1;
        }

        return ret;
    }

}
