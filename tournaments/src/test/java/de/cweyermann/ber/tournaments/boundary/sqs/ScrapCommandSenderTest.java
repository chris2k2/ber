package de.cweyermann.ber.tournaments.boundary.sqs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;

public class ScrapCommandSenderTest {

    private ScrapCommandSender sender;
    private Repository repo;

    @Before
    public void init() {
        sender = new ScrapCommandSender();
        repo = mock(Repository.class);
        sender.repo = repo;
    }

    @Test
    public void doesnotexistbefore_returned() {
        expectIsThere(new Tournament());
    }

    private void expectIsThere(Tournament t) {
        assertEquals(Arrays.asList(t), sender.filter(Arrays.asList(t)));
    }

    @Test
    public void existsAndDone_notReturned() {
        Tournament t = new Tournament();
        t.setId("asdf");

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.DONE);

        when(repo.findById("asdf")).thenReturn(Optional.of(dbT));

        expectIsNotThere(t);
    }

    private void expectIsNotThere(Tournament t) {
        assertEquals(Arrays.asList(), sender.filter(Arrays.asList(t)));
    }

    @Test
    public void existsAndUnprocessed_returned() {
        Tournament t = new Tournament();
        t.setId("asdf");

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.UNPROCESSED);

        when(repo.findById("asdf")).thenReturn(Optional.of(dbT));

        expectIsThere(t);
    }

    @Test
    public void works4two() {
        Tournament t = new Tournament();
        t.setId("asdf");

        Tournament t2 = new Tournament();
        t2.setId("asdf2");

        DynmoDbTournament dbT = new DynmoDbTournament();
        dbT.setStatus(ProccessingStatus.UNPROCESSED);

        DynmoDbTournament dbT2 = new DynmoDbTournament();
        dbT2.setStatus(ProccessingStatus.UNPROCESSED);

        when(repo.findById("asdf")).thenReturn(Optional.of(dbT));
        when(repo.findById("asdf2")).thenReturn(Optional.of(dbT2));

        assertEquals(Arrays.asList(t, t2), sender.filter(Arrays.asList(t, t2)));
    }
}