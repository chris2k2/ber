package de.cweyermann.ber.ratings.boundary;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.cweyermann.ber.ratings.entity.Player;


@FeignClient("players")
public interface PlayersClient {

    @RequestMapping(method = RequestMethod.GET, value = "/players/{id}")
    public Player getSingle(@PathVariable("id") String id);
    
    @RequestMapping(method = RequestMethod.GET, value = "/players/")
    public Iterable<Player> getAll();

    @RequestMapping(method = RequestMethod.PUT, value = "/players/{id}")
    public void update(@PathVariable("id") String id, @RequestBody Player player);
}
