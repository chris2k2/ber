package de.cweyermann.ber.playerratings.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.cweyermann.ber.playerratings.control.Elo.DoublesStrategy;
import de.cweyermann.ber.playerratings.control.Elo.InitStrategy;
import de.cweyermann.ber.playerratings.control.Elo.KStrategy;
import de.cweyermann.ber.playerratings.control.Elo.ResultStrategy;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Match.Player;
import de.cweyermann.ber.playerratings.entity.Result;

public class EloStrategies {

    public static final ResultStrategy SIMPLE_WIN_LOOSE = new WeigtendResult(1.0);

    public static final InitStrategy EVERYONE_1500 = (x, y) -> 1500;

    public static final KStrategy K_CONST8 = x -> 8;

    public static final DoublesStrategy AVERAGE = new WeightendAverageDoubles(0.5, 0.5);

    public static class OponentRatingWithFallback implements InitStrategy {

        private final InitStrategy fallback;

        public OponentRatingWithFallback(InitStrategy fallback) {
            this.fallback = fallback;
        }

        public int getInitialScoreByMatch(Match match, Player player) {
            List<Player> oponents = match.getHomePlayers();

            long count = match.getHomePlayers()
                    .stream()
                    .map(p -> p.getId())
                    .filter(id -> id.equals(player.getId()))
                    .count();
            if (count > 0) {
                oponents = match.getAwayPlayers();
            }

            Optional<Player> weekOp = oponents.stream()
                    .filter(p -> p.getOldRating() != null)
                    .sorted((p1, p2) -> p1.getOldRating().compareTo(p2.getOldRating()))
                    .findFirst();

            if (weekOp.isPresent()) {
                return weekOp.get().getOldRating();
            } else {
                return fallback.getInitialScoreByMatch(match, player);
            }
        }
    }

    public static class LeagueDepth implements InitStrategy {

        public static Map<Integer, Integer> map = new HashMap<>();

        private int factor;

        private boolean withFallback;

        public LeagueDepth(int factor) {
            this(factor, false);
        }

        public LeagueDepth(int factor, boolean withFallback) {
            this.factor = factor;
            this.withFallback = withFallback;
            for (int i = 0; i < 11; i++) {
                map.put(i, 0);
            }
        }

        @Override
        public int getInitialScoreByMatch(Match match, Player player) {
            String league = match.getLeague();

            int depth = 9;
            if (league != null) {
                depth = check(league, depth, "1. Bundesliga", 1);
                depth = check(league, depth, "2. Bundesliga", 2);
                depth = check(league, depth, "Regionalliga", 3);
                depth = check(league, depth, "Bayernliga", 4);
                depth = check(league, depth, "Bezirksoberliga", 5);
                depth = check(league, depth, "Bezirksliga", 6);
                depth = check(league, depth, "Bezirksklasse A", 7);
                depth = check(league, depth, "Bezirksklasse B", 8);
            }
            map.put(depth, map.get(depth).intValue() + 1);

            if (withFallback) {
                if (depth == 9 && match.getHomePlayers().size() == 1) {
                    if (player.getId().equals(match.getHomePlayers().get(0).getId())
                            && match.getAwayPlayers().get(0).getOldRating() != null) {
                        return match.getAwayPlayers().get(0).getOldRating();
                    } else if (player.getId().equals(match.getAwayPlayers().get(0).getId())
                            && match.getHomePlayers().get(0).getOldRating() != null) {
                        return match.getHomePlayers().get(0).getOldRating();
                    }
                }
                map.put(10, map.get(depth).intValue() + 1);
            }

            return 1800 - depth * factor;
        }

        private int check(String league, int depth, String name, int newDepth) {
            if (league.contains(name) && !league.contains("U1")) {
                depth = newDepth;
            }
            return depth;
        }
    }

    public static class WeigtendResult implements ResultStrategy {

        private double threeSetWinForHome;

        public WeigtendResult(double threeSetWinForHome) {
            this.threeSetWinForHome = threeSetWinForHome;
        }

        @Override
        public double getScoreModifier(Result result) {

            switch (result) {
            case AWAY_2_SET:
                return 0;
            case AWAY_3_SET:
                return 1 - threeSetWinForHome;
            case HOME_2_SET:
                return 1;
            case HOME_3_SET:
                return threeSetWinForHome;
            case NO_RESULT:
            default:
                return 0.5;
            }

        }

    }

    public static class WeightendAverageDoubles implements DoublesStrategy {
        private double strongerFactor;
        private double weakerFactor;

        public WeightendAverageDoubles(double strongerFactor, double weakerFactor) {
            this.strongerFactor = strongerFactor;
            this.weakerFactor = weakerFactor;
        }

        @Override
        public int getEloRatingForDoubles(int rating1, int rating2) {
            int strong = rating1;
            int weak = rating2;

            if (rating2 > rating1) {
                strong = rating2;
                weak = rating1;
            }

            return (int) (strong * strongerFactor + weak * weakerFactor);
        }

    }

    public static class KBasedOnOldRating implements KStrategy {

        private int gmLevel;
        private int jugendLevel;
        private int gmK;
        private int jugendK;
        private int k;

        public KBasedOnOldRating(int gmLevel, int jugendLevel, int gmK, int jugendK, int k) {
            this.gmLevel = gmLevel;
            this.jugendLevel = jugendLevel;
            this.gmK = gmK;
            this.jugendK = jugendK;
            this.k = k;
        }

        /**
         * chess defaults
         */
        public KBasedOnOldRating() {
            this(2400, 1600, 10, 40, 20);
        }

        @Override
        public int getKFactor(Match match) {
            List<Player> players = new ArrayList<>();
            players.addAll(match.getHomePlayers());
            players.addAll(match.getAwayPlayers());

            List<Integer> ratings = players.stream().map(p -> p.getOldRating()).sorted().collect(
                    Collectors.toList());

            int result = k;
            Integer first = ratings.get(0);
            Integer last = ratings.get(ratings.size() - 1);
            if (first != null && first < jugendLevel) {
                result = jugendK;
            } else if (last != null && last > gmLevel) {
                result = gmK;
            }

            return result;
        }
    }
}
