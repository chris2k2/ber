package de.cweyermann.ber.players.boundary;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.cweyermann.ber.players.entity.Match;

@FeignClient("matches")
public interface MatchesClient {

    @RequestMapping(method = RequestMethod.GET, value = "/matches")
    public List<Match> getAllForPlayer(
            @RequestParam(name = "playerid", required = false) String id);
}
