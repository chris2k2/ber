package de.cweyermann.ber.playerratings.control;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.entity.Player.Sex;
import de.cweyermann.ber.playerratings.entity.Match.Discipline;

public class GuessDisciplineTest {

    @Test
    public void ffff_wd() {
        assertEquals(Discipline.WD, match(Sex.F, Sex.F, Sex.F, Sex.F));
    }

    @Test
    public void mmmm_md() {
        assertEquals(Discipline.MD, match(Sex.M, Sex.M, Sex.M, Sex.M));
    }

    @Test
    public void mffm_mx() {
        assertEquals(Discipline.MX, match(Sex.M, Sex.F, Sex.F, Sex.M));
    }

    @Test
    public void uffm_mx() {
        assertEquals(Discipline.MX, match(Sex.UNKNOWN, Sex.F, Sex.F, Sex.M));
    }

    @Test
    public void ufum_mx() {
        assertEquals(Discipline.MX, match(Sex.UNKNOWN, Sex.F, Sex.UNKNOWN, Sex.M));
    }

    @Test
    public void mu_ms() {
        assertEquals(Discipline.MS, match(Sex.UNKNOWN, Sex.M));
    }

    @Test
    public void ff_ws() {
        assertEquals(Discipline.WS, match(Sex.F, Sex.F));
    }

    @Test
    public void uu_null() {
        assertEquals(null, match(Sex.UNKNOWN, Sex.UNKNOWN));
    }

    private Discipline match(Sex... sexes) {
        List<Player> players = new ArrayList<>();

        for (Sex sex : sexes) {
            Player p = new Player();
            p.setSex(sex);
            players.add(p);
        }

        return new GuessDiscipline().fromPlayersCombination(players);
    }
}
