package de.cweyermann.ber.playerratings.entity;

public enum Result {
    HOME_3_SET, HOME_2_SET, AWAY_3_SET, AWAY_2_SET, NO_RESULT;

    public static Result fromResultString(String result) {

        int homeSets = 0;
        int awaySets = 0;

        String[] sets = result.split(" ");
        for (String set : sets) {
            String[] points = set.split(":");
            int homePoints = Integer.parseInt(points[0]);
            int awayPoints = Integer.parseInt(points[1]);

            if (homePoints > awayPoints) {
                homeSets++;
            } else {
                awaySets++;
            }
        }

        Result res = null;
        if (homeSets > awaySets) {
            res = Result.HOME_3_SET;
        } else {
            res = Result.AWAY_3_SET;
        }

        return res;
    }
}