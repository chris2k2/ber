package de.cweyermann.ber.ratings.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.cweyermann.ber.ratings.boundary.PlayersClient;
import de.cweyermann.ber.ratings.boundary.Repository;
import de.cweyermann.ber.ratings.control.Elo.InitStrategy;
import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Match.Status;
import de.cweyermann.ber.ratings.entity.Player;
import de.cweyermann.ber.ratings.entity.Result;

public class RatingTest {


    public static final InitStrategy EVERYONE_300 = (x, y) -> 300;
    
    private PlayersClient client;
    private Map<String, Player> map;

    @Before
    public void setup() {
        map = new HashMap<>();

        client = new PlayersClient() {

            @Override
            public void update(String id, Player player) {
                map.put(id, player);
            }

            @Override
            public Player getSingle(String id) {
                return map.get(id);
            }

            @Override
            public Iterable<Player> getAll() {
                return () -> map.values().iterator();
            }
        };

        client.update("1", build("1", 1000, 1000));
        client.update("2", build("2", 1000, 1000));
        client.update("11", build("11", 1000, 1000));
        client.update("12", build("12", 1000, 500));
        client.update("21", build("21", 1000, 1000));
        client.update("22", build("22", 1000, 500));
    }

    private Player build(String id, int ratingSingles, int ratingDoubles) {
        Player p1 = new Player();
        p1.setId(id);

        p1.setRatingSingles(ratingSingles);
        p1.setRatingDoubles(ratingDoubles);
        return p1;
    }

    @Test
    public void singlesHomePlayer_won() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:10", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

        assertTrue(match.getHomePlayers().get(0).getAfterRating() > 1000);
        assertTrue(match.getAwayPlayers().get(0).getAfterRating() < 1000);
    }

    @Test
    public void singlesAwayPlayer_won() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

        assertTrue(match.getHomePlayers().get(0).getAfterRating() < 1000);
        assertTrue(match.getAwayPlayers().get(0).getAfterRating() > 1000);
    }

    @Test
    public void playDoubles() {
        Repository repo = mock(Repository.class);
        Match match = playDoubles("21:10 21:23 11:21", repo);
        mockRepo(repo, match);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

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
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

        assertEquals(Status.RATED, match.getProcessStatus());
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

        Rating rating = new Rating(repo, elo, client);
        rating.updateUnratedMatches();

        assertEquals(1010, match.getHomePlayers().get(0).getAfterRating().intValue());
        assertEquals(990, match.getAwayPlayers().get(0).getAfterRating().intValue());
    }
    

    @Test
    public void singlesNoRating_initCalled() {
        Repository repo = mock(Repository.class);
        Match match = playSingles("11:8 11:13 8:11 11:2 11:2", repo);
        mockRepo(repo, match);

        map.clear();

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EVERYONE_300, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

        assertTrue(match.getHomePlayers().get(0).getAfterRating().intValue() > 300);
        assertTrue(match.getAwayPlayers().get(0).getAfterRating().intValue() < 300);
    }

    @Test
    public void doubles_eloIsUsedWithAverage() {
        Repository repo = mock(Repository.class);
        Match match = playDoubles("21:10 21:1", repo);
        mockRepo(repo, match);

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(-50);

        Rating rating = new Rating(repo, elo, client);
        rating.updateUnratedMatches();

        verify(elo).calcDoublesDifference(anyInt(), anyInt(), anyInt(), anyInt(), any(Result.class), any(Match.class));
    }

    @Test
    public void twoMatches_differentPlayers() {
        Repository repo = mock(Repository.class);

        Match match1 = playSingles("21:10 30:29", repo);
        Match match2 = playDoubles("21:10 21:23 21:2", repo);
        mockRepo(repo, match1, match2);

        Rating rating = new Rating(repo, new Elo(EloStrategies.SIMPLE_WIN_LOOSE,
                EloStrategies.EVERYONE_1000, EloStrategies.K_CONST8, EloStrategies.AVERAGE),
                client);
        rating.updateUnratedMatches();

        assertTrue(match1.getHomePlayers().get(0).getAfterRating() > 1000);
        assertTrue(match2.getHomePlayers().get(0).getAfterRating() > 1000);
    }

    @Test
    public void twoMatches_samePlayer() {
        Repository repo = mock(Repository.class);

        Match match1 = playSingles("21:10 30:29", repo);
        Match match2 = playSingles("21:10 30:29", repo);
        mockRepo(repo, match1, match2);

        Elo elo = mock(Elo.class);
        when(elo.calcDifference(anyInt(), anyInt(), any(Result.class), any(Match.class)))
                .thenReturn(-10);
        Rating rating = new Rating(repo, elo, client);
        rating.updateUnratedMatches();

        assertEquals(980, client.getSingle("1").getRatingSingles().intValue());
    }

    private Match playSingles(String result, Repository repo) {
        Match match = new Match();
        Match.Player player1 = new Match.Player();
        player1.setId("1");
        player1.setRating(1000);

        Match.Player player2 = new Match.Player();
        player2.setId("2");
        player2.setRating(1000);

        match.setHomePlayers(Arrays.asList(player1));
        match.setAwayPlayers(Arrays.asList(player2));
        match.setResult(result);
        match.setDiscipline("WS");

        return match;
    }
    

    private void mockRepo(Repository repo, Match... match) {
        when(repo.getAllUnratedMatches()).thenReturn(Arrays.asList(match));
    }

    private Match playDoubles(String result, Repository repo) {
        Match match = new Match();
        Match.Player player1 = new Match.Player();
        player1.setId("11");
        player1.setRating(1000);
        Match.Player player11 = new Match.Player();
        player11.setId("12");
        player11.setRating(500);

        Match.Player player2 = new Match.Player();
        player2.setId("21");
        player2.setRating(1000);
        Match.Player player21 = new Match.Player();
        player21.setId("22");
        player21.setRating(500);

        match.setHomePlayers(Arrays.asList(player1, player11));
        match.setAwayPlayers(Arrays.asList(player2, player21));
        match.setResult(result);
        match.setDiscipline("MD");
        return match;
    }

}
