package de.cweyermann.ber.tournaments.boundary.sqs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;

public class ScrapCommandSenderTest {

    private static final Date DEFAULT_DATE = new Date(10000l);
    private ScrapCommandSender sender;
    private Repository repo;

    @Before
    public void init() {
        sender = new ScrapCommandSender();
        repo = mock(Repository.class);
        sender.repo = repo;
    }

    private void expectIsThere(Tournament t) {
        assertEquals(Arrays.asList(t), sender.filter(Arrays.asList(t)));
    }

    @Test
    public void existsAndDone_notReturned() {
        Tournament t = new Tournament();
        t.setId("asdf");
        t.setEndDate(DEFAULT_DATE);

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.DONE);

        when(repo.findById("asdf", DEFAULT_DATE)).thenReturn(Optional.of(dbT));

        expectIsNotThere(t);
    }

    private void expectIsNotThere(Tournament t) {
        assertEquals(Arrays.asList(), sender.filter(Arrays.asList(t)));
    }

    @Test
    public void existsAndUnprocessed_returned() {
        Tournament t = new Tournament();
        t.setId("asdf");
        t.setEndDate(DEFAULT_DATE);

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.UNPROCESSED);

        when(repo.findById("asdf", DEFAULT_DATE)).thenReturn(Optional.of(dbT));

        expectIsThere(t);
    }

    @Test
    public void works4two() {
        Tournament t = new Tournament();
        t.setId("asdf");
        t.setEndDate(DEFAULT_DATE);

        Tournament t2 = new Tournament();
        t2.setId("asdf2");
        t2.setEndDate(DEFAULT_DATE);

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.UNPROCESSED);

        DynmoDbTournament dbT2 = new DynmoDbTournament();
        dbT2.setStatus(ProccessingStatus.UNPROCESSED);

        when(repo.findById("asdf", DEFAULT_DATE)).thenReturn(Optional.of(dbT));
        when(repo.findById("asdf2", DEFAULT_DATE)).thenReturn(Optional.of(dbT2));

        assertEquals(Arrays.asList(t, t2), sender.filter(Arrays.asList(t, t2)));
    }
}
