package de.cweyermann.ber.playerratings.control;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.cweyermann.ber.playerratings.boundary.Repository;
import de.cweyermann.ber.playerratings.control.DetectSex;
import de.cweyermann.ber.playerratings.entity.Match;
import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;
import de.cweyermann.ber.playerratings.entity.Player.Sex;

public class DetectSexTest {

    private DetectSex ds;

    private Repository repo;

    @Before
    public void init() {
        repo = mock(Repository.class);
        ds = new DetectSex(repo);
    }

    @Test
    public void not_test()
    {
        assertEquals(1, 0 ^ 1);
        assertEquals(0, 1 ^ 1);
    }
    
    @Test
    public void disciplineKnown_mapsToSpecificSex() {
        assertMapping(Sex.M, Discipline.MS);
        assertMapping(Sex.M, Discipline.MD);
        assertMapping(Sex.F, Discipline.WS);
        assertMapping(Sex.F, Discipline.WD);
    }

    @Test
    public void disciplineMixed_inverseOfPartner() {
        assertMySexOppositeOfPartner(Sex.M, Sex.F);
        assertMySexOppositeOfPartner(Sex.F, Sex.M);
    }


    @Test
    public void disciplineMixedallPermutations_inverseOfPartner() {
        assertMySexOppositeOfPartner(Sex.M, 1, 2, 3, 4, Sex.M, Sex.F, Sex.M, Sex.F);
        assertMySexOppositeOfPartner(Sex.F, 2, 1, 3, 4, Sex.M, Sex.F, Sex.M, Sex.F);
        assertMySexOppositeOfPartner(Sex.M, 2, 3, 1, 4, Sex.M, Sex.F, Sex.M, Sex.F);
        assertMySexOppositeOfPartner(Sex.F, 2, 4, 3, 1, Sex.M, Sex.F, Sex.M, Sex.F);

        assertMySexOppositeOfPartner(Sex.F, 1, 2, 3, 4, Sex.F, Sex.M, Sex.F, Sex.M);
        assertMySexOppositeOfPartner(Sex.M, 2, 1, 3, 4, Sex.F, Sex.M, Sex.F, Sex.M);
        assertMySexOppositeOfPartner(Sex.F, 2, 3, 1, 4, Sex.F, Sex.M, Sex.F, Sex.M);
        assertMySexOppositeOfPartner(Sex.M, 2, 4, 3, 1, Sex.F, Sex.M, Sex.F, Sex.M);
    }

    @Test
    public void partnerNotInDatabase()
    {
        Match m = new Match();
        m.setDiscipline(Discipline.MX);

        m.setHomePlayers(Arrays.asList(player("1"), player("2")));
        m.setAwayPlayers(Arrays.asList(player("3"), player("4")));

        Sex sex = ds.fromMatch("1", m);

        assertEquals(Sex.UNKNOWN, sex);
    }
    
    private void assertMySexOppositeOfPartner(Sex mysex, Sex partnersex) {
        assertMySexOppositeOfPartner(mysex, 1, 2, 3, 4, Sex.M, partnersex, Sex.M, Sex.F);
    }

    private void assertMySexOppositeOfPartner(Sex mySex, int id1, int id2, int id3,
            int id4, Sex s1, Sex s2, Sex s3, Sex s4) {
        Match m = new Match();
        m.setDiscipline(Discipline.MX);

        addPlayerToRepo(id1, s1);
        addPlayerToRepo(id2, s2);
        addPlayerToRepo(id3, s3);
        addPlayerToRepo(id4, s4);
        
        m.setHomePlayers(Arrays.asList(player(id1 + ""), player(id2 + "")));
        m.setAwayPlayers(Arrays.asList(player(id3 + ""), player(id4 + "")));

        Sex sex = ds.fromMatch("1", m);

        assertEquals(mySex, sex);
    }

    private void addPlayerToRepo(int id1, Sex s1) {
        Player player = new Player();
        player.setSex(s1);
        when(repo.findById(id1 + "")).thenReturn(Optional.of(player));
    }

    private Match.Player player(String id) {
        Match.Player p1 = new Match.Player();
        p1.setId(id);
        return p1;
    }

    private void assertMapping(Sex expected, Discipline discipline) {
        Match m = new Match();
        m.setDiscipline(discipline);

        Sex sex = ds.fromMatch("1", m);

        assertEquals(expected, sex);
    }

}
