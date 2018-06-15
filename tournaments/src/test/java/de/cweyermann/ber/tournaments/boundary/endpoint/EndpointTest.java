package de.cweyermann.ber.tournaments.boundary.endpoint;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.cweyermann.ber.tournaments.boundary.endpoint.Endpoint;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.entity.Tournament;

@RunWith(SpringRunner.class)
@WebMvcTest(Endpoint.class)
public class EndpointTest {

    private final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Repository repo;

    @Test
    public void findOne() throws Exception {
        performQuery("?source=s2").andExpect(jsonPath("$[0].id", is("4")))
                .andExpect(jsonPath("$[0].id", is("4")))
                .andExpect(jsonPath("$[0].name", is("name")))
                .andExpect(jsonPath("$[0].status", is("DONE")))
                .andExpect(jsonPath("$[0].endDate", is("2016-01-03T23:00:00.000+0000")))
                .andExpect(jsonPath("$[0].source", is("s2")));
    }

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
    

    @Test
    public void findAll_LatestSetButBeforeToday() throws Exception {
        assertIdsReturned("?source=s4latest=true", Collections.emptyList());
    }
    
    @Test
    public void post() throws Exception
    {
        Tournament anObject = new Tournament();
        anObject.setEndDate(new Date(0));
        anObject.setId("id");
        anObject.setName("name");
        anObject.setSource("source");
        anObject.setStatus(de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus.DONE);

        String requestJson = convert2Json(anObject);
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/tournaments")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(status().isCreated()).andReturn();
        
        DynmoDbTournament dynamoT = new DynmoDbTournament();
        dynamoT.setEndDate(new Date(0));
        dynamoT.setId("id");
        dynamoT.setName("name");
        dynamoT.setSource("source");
        dynamoT.setStatus(ProccessingStatus.DONE);
        
        verify(repo).saveAll(Arrays.asList(dynamoT));
    }

    @Test
    public void post_unprocessedIsIgnore() throws Exception
    {
        Tournament anObject = new Tournament();
        anObject.setEndDate(new Date(0));
        anObject.setId("id");
        anObject.setName("name");
        anObject.setSource("source");
        de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus status = de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus.DONE;
        anObject.setStatus(status);
    
        String requestJson = convert2Json(anObject);
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/tournaments")
                .content(requestJson)
                .contentType(MediaType.APPLICATION_JSON);
    
        DynmoDbTournament dynamoT = defTournament(ProccessingStatus.DONE);
        when(repo.findById("id")).thenReturn(Optional.of(dynamoT));
        
        mockMvc.perform(requestBuilder).andExpect(status().isCreated()).andReturn();
        
        verify(repo, times(0)).saveAll(any());
    }

    private String convert2Json(Tournament anObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(new Tournament[] {anObject});
        return requestJson;
    }
    
    
    private DynmoDbTournament defTournament(ProccessingStatus done) {
        DynmoDbTournament dynamoT = new DynmoDbTournament();
        dynamoT.setEndDate(new Date(0));
        dynamoT.setId("id");
        dynamoT.setName("name");
        dynamoT.setSource("source");
        dynamoT.setStatus(done);
        return dynamoT;
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
        DynmoDbTournament t1 = tournament("1", "20160101", "s1", ProccessingStatus.DONE);
        DynmoDbTournament t2 = tournament("2", "20160103", "s1", ProccessingStatus.DONE);
        DynmoDbTournament t3 = tournament("3", "20160109", "s1", ProccessingStatus.DOING);
        DynmoDbTournament t4 = tournament("4", "20160104", "s2", ProccessingStatus.DONE);
        DynmoDbTournament t5 = tournament("5", "20160112", "s3", ProccessingStatus.UNPROCESSED);
        DynmoDbTournament t6 = tournament("6", "20190112", "s4", ProccessingStatus.DONE);

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
        
        when(repo.findAllBySource("s4")).thenReturn(Arrays.asList(t6));
    }

    private DynmoDbTournament tournament(String id, String endDate, String source,
            ProccessingStatus status) throws ParseException {
        DynmoDbTournament t1 = new DynmoDbTournament();
        t1.setId(id);
        t1.setEndDate(FORMAT.parse(endDate));
        t1.setStatus(status);
        t1.setSource(source);
        t1.setName("name");

        return t1;
    }
}