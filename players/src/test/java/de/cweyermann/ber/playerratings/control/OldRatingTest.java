package de.cweyermann.ber.playerratings.control;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.control.OldRating;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;

public class OldRatingTest {

    private OldRating addOldRating;
    private Repository repo;

    @Before
    public void setup() {
        addOldRating = new OldRating();
        repo = mock(Repository.class);
        addOldRating.repo = repo;
        addOldRating.guessDiscipline = new GuessDiscipline() {
            @Override
            public Discipline fromMatch(Match m) {
                return m.getDiscipline();
            }
        };
    }

    @Test
    public void notFound_noRating() throws JsonParseException, JsonMappingException, IOException
    {
        when(repo.findById("123")).thenReturn(Optional.empty());
        
        Match.Player defaultPlayer = matchDefault(Discipline.MS);
        
        assertEquals(null, defaultPlayer.getOldRating());
    }


    @Test
    public void singlesRatingNotSingles_Null() throws JsonParseException, JsonMappingException, IOException
    {
        play(null, null, null, null, Discipline.MS);
    }
    

    @Test
    public void singlesRating_used() throws JsonParseException, JsonMappingException, IOException
    {
        play(1000, null, 1000, null, Discipline.MS);
    }
    

    @Test
    public void doublesRating_used() throws JsonParseException, JsonMappingException, IOException
    {
        play(200, 200, 1, 300, Discipline.WD);
    }
    

    @Test
    public void mixedRating_used() throws JsonParseException, JsonMappingException, IOException
    {
        play(300, 1, 1000, 300, Discipline.MX);
    }

    private void play(Integer expect, Integer doublesRating, Integer singlesRating,
            Integer mixedRating, Discipline discipline)
            throws JsonParseException, JsonMappingException, IOException {
        Player player = new Player();
        player.setRatingDoubles(doublesRating);
        player.setRatingSingles(singlesRating);
        player.setRatingMixed(mixedRating);
        when(repo.findById("123")).thenReturn(Optional.of(player));
        
        Match.Player defaultPlayer = matchDefault(discipline);
        
        assertEquals(expect, defaultPlayer.getOldRating());
    }
    
    private Match.Player matchDefault(Discipline d) throws JsonParseException, JsonMappingException, IOException {
        Match m1 = new Match();
        m1.setDiscipline(d);
        Match.Player p1 = new Match.Player();
        p1.setId("123");
        m1.setHomePlayers(Arrays.asList(p1));
        m1.setAwayPlayers(Collections.emptyList());
        addOldRating.addIfKnown(m1);
        
        return p1;
    }

}
