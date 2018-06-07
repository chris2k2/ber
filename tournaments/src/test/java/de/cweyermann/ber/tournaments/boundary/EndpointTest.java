package de.cweyermann.ber.tournaments.boundary;

import static de.cweyermann.ber.tournaments.TournamentBuilder.tournament;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import de.cweyermann.ber.tournaments.entity.Tournament;
import de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus;

@RunWith(SpringRunner.class)
@WebMvcTest(Endpoint.class)
public class EndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Repository repo;

    @Test
    public void findAll() throws Exception {
        assertIdsReturned("", Arrays.asList("1", "2", "3", "4", "5"));
    }

    @Test
    public void findAll_Done() throws Exception {
        assertIdsReturned("?status=DONE", Arrays.asList("1", "2", "4"));
    }

    @Test
    public void findAll_DoneAndSource() throws Exception {
        assertIdsReturned("?status=DONE&source=s1", Arrays.asList("1", "2"));
    }

    @Test
    public void findAll_Source() throws Exception {
        assertIdsReturned("?source=s2", Arrays.asList("4"));
    }

    @Test
    public void findAll_DoneSourceAndLatestSet() throws Exception {
        assertIdsReturned("?status=DONE&source=s2&latest=true", Arrays.asList("4"));
    }

    @Test
    public void findAll_latestDoesNotGiveResult() throws Exception {
        assertIdsReturned("?status=DONE&source=s3&latest=true", Arrays.asList());
    }

    @Test
    public void findAll_DoingAndLatestSet() throws Exception {
        assertIdsReturned("?status=DOING&latest=true", Arrays.asList("2"));
    }

    @Test
    public void findAll_SourceAndLatestSet() throws Exception {
        assertIdsReturned("?source=s1&latest=true", Arrays.asList("3"));
    }

    @Test
    public void findAll_LatestSet() throws Exception {
        assertIdsReturned("?latest=true", Arrays.asList("5"));
    }

    private void assertIdsReturned(String queryString, List<String> ids) throws Exception {

        int size = ids.size();
        ResultActions resultActions = performQuery(queryString)
                .andExpect(jsonPath("$", hasSize(size)));

        for (int i = 0; i < ids.size(); i++) {
            resultActions.andExpect(jsonPath("$[" + i + "].id", is(ids.get(i))));
        }
    }

    private ResultActions performQuery(String queryString) throws Exception {
        mockDefaultDataSet();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/tournaments" + queryString)
                .accept(MediaType.APPLICATION_JSON);

        return mockMvc.perform(requestBuilder);
    }

    private void mockDefaultDataSet() throws ParseException {
        Tournament t1 = tournament("1", "20160101", "s1", ProccessingStatus.DONE);
        Tournament t2 = tournament("2", "20160103", "s1", ProccessingStatus.DONE);
        Tournament t3 = tournament("3", "20160109", "s1", ProccessingStatus.DOING);
        Tournament t4 = tournament("4", "20160104", "s2", ProccessingStatus.DONE);
        Tournament t5 = tournament("5", "20160112", "s3", ProccessingStatus.UNPROCESSED);

        when(repo.findAll()).thenReturn(Arrays.asList(t1, t2, t3, t4, t5));

        when(repo.findByStatus(ProccessingStatus.DONE)).thenReturn(Arrays.asList(t1, t2, t4));

        when(repo.findAllBySource("s1")).thenReturn(Arrays.asList(t1, t2, t3));
        when(repo.findAllBySource("s2")).thenReturn(Arrays.asList(t4));

        when(repo.findByStatusAndSource(ProccessingStatus.DONE, "s1"))
                .thenReturn(Arrays.asList(t1, t2));

        when(repo.findFirstBySourceAndStatusOrderByEndDateDesc("s1", ProccessingStatus.DONE))
                .thenReturn(Optional.of(t2));
        when(repo.findFirstBySourceAndStatusOrderByEndDateDesc("s2", ProccessingStatus.DONE))
                .thenReturn(Optional.of(t4));

        when(repo.findFirstBySourceAndStatusOrderByEndDateDesc("s3", ProccessingStatus.DONE))
                .thenReturn(Optional.empty());

        when(repo.findFirstBySourceOrderByEndDateDesc("s1")).thenReturn(Optional.of(t3));

        when(repo.findFirstByStatusOrderByEndDateDesc(ProccessingStatus.DOING))
                .thenReturn(Optional.of(t2));

        when(repo.findFirstOrderByEndDateDesc()).thenReturn(Optional.of(t5));
    }

}