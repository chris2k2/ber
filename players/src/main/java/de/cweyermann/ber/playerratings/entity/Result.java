package de.cweyermann.ber.playerratings.entity;

public enum Result {
    HOME_3_SET, HOME_2_SET, AWAY_3_SET, AWAY_2_SET, NO_RESULT;

    public static Result fromResultString(String result) {
        try {
            return fromResultStringWithException(result);
        } catch (Exception e) {
            return Result.NO_RESULT;
        }
    }

    private static Result fromResultStringWithException(String result) {
        result = result.replaceAll("[-]", ":");

        int homeSets = 0;
        int awaySets = 0;

        String[] sets = result.split(" ");
        if (sets.length >= 2) {
            for (String set : sets) {
                String[] points = set.split(":");
                if (points.length == 2) {
                    int homePoints = Integer.parseInt(points[0]);
                    int awayPoints = Integer.parseInt(points[1]);

                    if (homePoints > awayPoints) {
                        homeSets++;
                    } else {
                        awaySets++;
                    }
                }
            }
        }

        Result res = Result.NO_RESULT;
        if (homeSets > awaySets && awaySets == 0) {
            res = Result.HOME_2_SET;
        } else if (homeSets > awaySets && awaySets > 0) {
            res = Result.HOME_3_SET;
        } else if (homeSets < awaySets && homeSets == 0) {
            res = Result.AWAY_2_SET;
        } else if (homeSets < awaySets && homeSets > 0) {
            res = Result.AWAY_3_SET;
        }

        return res;
    }
}