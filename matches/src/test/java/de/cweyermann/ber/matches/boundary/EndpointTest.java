package de.cweyermann.ber.matches.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.matches.boundary.persistence.Repository;

@RunWith(SpringRunner.class)
@WebMvcTest(Endpoint.class)
public class EndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Repository repo;

    @Test
    public void ping_pong() throws Exception {

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/ping")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals("pong", result.getResponse().getContentAsString());
    }

    @Test
    public void unknownMatch_404() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/matches/123")
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(404, result.getResponse().getStatus());
    }

    @Test
    public void knownMatch_mapped() throws Exception {
        DynamoDbMatch dynamoDbMatch = new DynamoDbMatch();
        dynamoDbMatch.setLeague("my league");
        when(repo.findById("123")).thenReturn(Optional.of(dynamoDbMatch));
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/matches/123")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(jsonPath("league", is("my league")));
    }

    @Test
    public void playerIdGiven_takenIntoAccount() throws Exception
    {
        DynamoDbMatch dynamoDbMatch1 = new DynamoDbMatch();
        dynamoDbMatch1.setPlayer1Id("1");
        
        DynamoDbMatch dynamoDbMatch2 = new DynamoDbMatch();
        dynamoDbMatch2.setPlayer1Id("2");

        when(repo.findAll()).thenReturn(Arrays.asList(dynamoDbMatch1, dynamoDbMatch2));
        when(repo.findByAnyPlayerId("1")).thenReturn(Arrays.asList(dynamoDbMatch1));
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/matches?playerid=1")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(jsonPath("size()", is(1)));
    }
    

    @Test
    public void noPlayerIdGiven_moreResults() throws Exception
    {
        DynamoDbMatch dynamoDbMatch1 = new DynamoDbMatch();
        dynamoDbMatch1.setPlayer1Id("1");
        
        DynamoDbMatch dynamoDbMatch2 = new DynamoDbMatch();
        dynamoDbMatch2.setPlayer1Id("2");

        when(repo.findAll()).thenReturn(Arrays.asList(dynamoDbMatch1, dynamoDbMatch2));
        when(repo.findByAnyPlayerId("1")).thenReturn(Arrays.asList(dynamoDbMatch1));
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/matches")
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder).andExpect(jsonPath("size()", is(2)));
    }
}
