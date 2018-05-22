package de.cweyermann.ber.ratings.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import de.cweyermann.ber.ratings.boundary.Repository;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.EmbeededMatchPlayer;
import de.cweyermann.ber.ratings.entity.Result;

public class RatingTest {

    @Test
    public void singlesHomePlayer_won() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:10", repo);
        mockRepo(repo, match);
        
        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8));
        rating.addAll();

        assertTrue(match.getHomePlayers().get(0).getAfterRating() > 1000);
        assertTrue(match.getAwayPlayers().get(0).getAfterRating() < 1000);
    }

    @Test
    public void singlesAwayPlayer_won() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8));
        rating.addAll();

        assertTrue(match.getHomePlayers().get(0).getAfterRating() < 1000);
        assertTrue(match.getAwayPlayers().get(0).getAfterRating() > 1000);
    }

    @Test
    public void playDoubles() {
        Repository repo = mock(Repository.class);
        Match match = playDoubles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8));
        rating.addAll();

        assertTrue(match.getHomePlayers().get(0).getAfterRating() < 1000);
        assertTrue(match.getHomePlayers().get(1).getAfterRating() < 500);

        assertTrue(match.getAwayPlayers().get(0).getAfterRating() > 1000);
        assertTrue(match.getAwayPlayers().get(1).getAfterRating() > 500);
    }

    @Test
    public void singlesAwayPlayer_persisted() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8));
        rating.addAll();

        verify(repo).save(match);
    }

    @Test
    public void singles_neighboursUsed() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(10);

        Rating rating = new Rating(repo, elo);
        rating.addAll();

        assertEquals(1010, match.getHomePlayers().get(0).getAfterRating().intValue());
        assertEquals(990, match.getAwayPlayers().get(0).getAfterRating().intValue());
    }

    @Test
    public void doubles_eloIsUsedWithAverage() {
        Repository repo = mock(Repository.class);
        Match match = playDoubles("21:10 21:1", repo);
        mockRepo(repo, match);

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(-50);

        Rating rating = new Rating(repo, elo);
        rating.addAll();

        verify(elo).calcDifference(eq(750), eq(750), any(Result.class), any(Match.class));
    }

    @Test
    public void twoMatches_differentPlayers() {
        Repository repo = mock(Repository.class);
        
        Match match1 = playSingles("21:10 30:29", repo);
        Match match2 = playDoubles("21:10 21:23 21:2", repo);
        mockRepo(repo, match1, match2);
        
        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8));
        rating.addAll();

        assertTrue(match1.getHomePlayers().get(0).getAfterRating() > 1000);
        assertTrue(match2.getHomePlayers().get(0).getAfterRating() > 1000);
    }

    private Match playSingles(String result, Repository repo) {
        Match match = new Match();
        EmbeededMatchPlayer player1 = new EmbeededMatchPlayer();
        player1.setId("1");
        player1.setRating(1000);

        EmbeededMatchPlayer player2 = new EmbeededMatchPlayer();
        player2.setId("2");
        player2.setRating(1000);

        match.setHomePlayers(Arrays.asList(player1));
        match.setAwayPlayers(Arrays.asList(player2));
        match.setResult(result);

        return match;
    }

    private void mockRepo(Repository repo, Match... match) {
        when(repo.getAllUnratedMatches()).thenReturn(Arrays.asList(match));
    }

    private Match playDoubles(String result, Repository repo) {
        Match match = new Match();
        EmbeededMatchPlayer player1 = new EmbeededMatchPlayer();
        player1.setId("11");
        player1.setRating(1000);
        EmbeededMatchPlayer player11 = new EmbeededMatchPlayer();
        player11.setId("12");
        player11.setRating(500);

        EmbeededMatchPlayer player2 = new EmbeededMatchPlayer();
        player2.setId("21");
        player2.setRating(1000);
        EmbeededMatchPlayer player21 = new EmbeededMatchPlayer();
        player21.setId("22");
        player21.setRating(500);

        match.setHomePlayers(Arrays.asList(player1, player11));
        match.setAwayPlayers(Arrays.asList(player2, player21));
        match.setResult(result);
        return match;
    }

}
