package de.cweyermann.ber.ratings.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import de.cweyermann.ber.ratings.control.Elo.InitStrategy;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Match.Player;
import de.cweyermann.ber.ratings.entity.Result;

public class RatingTest {

    public static final InitStrategy EVERYONE_300 = (x, y) -> 300;

    private Player build(String id, int ratingSingles, int ratingDoubles) {
        Player p1 = new Player();
        p1.setId(id);
        p1.setOldRating(ratingSingles);

        return p1;
    }

    @Test
    public void singlesHomePlayer_won() {

        Match match = playSingles("21:10 21:10");

        Rating rating = new Rating(new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE));
        rating.updateUnratedMatches(Arrays.asList(match));

        assertTrue(match.getHomePlayers().get(0).getNewRating() > 1000);
        assertTrue(match.getAwayPlayers().get(0).getNewRating() < 1000);
    }

    @Test
    public void singlesAwayPlayer_won() {

        Match match = playSingles("21:10 21:23 11:21");

        Rating rating = new Rating(new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE));
        rating.updateUnratedMatches(Arrays.asList(match));

        assertTrue(match.getHomePlayers().get(0).getNewRating() < 1000);
        assertTrue(match.getAwayPlayers().get(0).getNewRating() > 1000);
    }

    @Test
    public void playDoubles() {

        Match match = playDoubles("21:10 21:23 11:21");

        Rating rating = new Rating(new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE));
        rating.updateUnratedMatches(Arrays.asList(match));

        assertTrue(match.getHomePlayers().get(0).getNewRating() < 1000);
        assertTrue(match.getHomePlayers().get(1).getNewRating() < 500);

        assertTrue(match.getAwayPlayers().get(0).getNewRating() > 1000);
        assertTrue(match.getAwayPlayers().get(1).getNewRating() > 500);
    }

    @Test
    public void singles_neighboursUsed() {

        Match match = playSingles("21:10 21:23 11:21");

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(10);

        Rating rating = new Rating(elo);
        rating.updateUnratedMatches(Arrays.asList(match));

        assertEquals(1010, match.getHomePlayers().get(0).getNewRating().intValue());
        assertEquals(990, match.getAwayPlayers().get(0).getNewRating().intValue());
    }

    @Test
    public void singlesNoRating_initCalled() {

        Match match = playSingles("11:8 11:13 8:11 11:2 11:2", null);

        Rating rating = new Rating(new Elo(EloStrategies.SIMPLE_WIN_LOOSE, EVERYONE_300,
                EloStrategies.K_CONST8, EloStrategies.AVERAGE));
        rating.updateUnratedMatches(Arrays.asList(match));

        assertTrue(match.getHomePlayers().get(0).getNewRating().intValue() > 300);
        assertTrue(match.getAwayPlayers().get(0).getNewRating().intValue() < 300);
    }

    @Test
    public void doubles_eloIsUsedWithAverage() {

        Match match = playDoubles("21:10 21:1");

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(-50);

        Rating rating = new Rating(elo);
        rating.updateUnratedMatches(Arrays.asList(match));

        verify(elo).calcDoublesDifference(anyInt(), anyInt(), anyInt(), anyInt(), any(Result.class),
                any(Match.class));
    }

    @Test
    public void twoMatches_differentPlayers() {

        Match match1 = playSingles("21:10 30:29");
        Match match2 = playDoubles("21:10 21:23 21:2");

        Rating rating = new Rating(new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE));
        rating.updateUnratedMatches(Arrays.asList(match1, match2));

        assertTrue(match1.getHomePlayers().get(0).getNewRating() > 1000);
        assertTrue(match2.getHomePlayers().get(0).getNewRating() > 1000);
    }

    private Match playSingles(String result) {
        return playSingles(result, 1000);
    }

    private Match playSingles(String result, Integer defaultRating) {
        Match match = new Match();
        Match.Player player1 = new Match.Player();
        player1.setId("1");
        player1.setOldRating(defaultRating);

        Match.Player player2 = new Match.Player();
        player2.setId("2");
        player2.setOldRating(defaultRating);

        match.setHomePlayers(Arrays.asList(player1));
        match.setAwayPlayers(Arrays.asList(player2));
        match.setResult(result);
        match.setDiscipline("WS");

        return match;
    }

    private Match playDoubles(String result) {
        Match match = new Match();
        Match.Player player1 = new Match.Player();
        player1.setId("11");
        player1.setOldRating(1000);
        Match.Player player11 = new Match.Player();
        player11.setId("12");
        player11.setOldRating(500);

        Match.Player player2 = new Match.Player();
        player2.setId("21");
        player2.setOldRating(1000);
        Match.Player player21 = new Match.Player();
        player21.setId("22");
        player21.setOldRating(500);

        match.setHomePlayers(Arrays.asList(player1, player11));
        match.setAwayPlayers(Arrays.asList(player2, player21));
        match.setResult(result);
        match.setDiscipline("MD");
        return match;
    }

}
