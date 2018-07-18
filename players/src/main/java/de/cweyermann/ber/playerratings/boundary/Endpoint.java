package de.cweyermann.ber.playerratings.boundary;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.cweyermann.ber.playerratings.entity.Player;
import de.cweyermann.ber.playerratings.errors.UnknownPlayerException;

@RestController
public class Endpoint {

    @Autowired
    private Repository repo;

    @GetMapping("players/{id}")
    public Player getSingle(@PathVariable("id") String id) {
        Optional<Player> dto = repo.findById(id);
        if (!dto.isPresent()) {
            throw new UnknownPlayerException(id);
        }

        return dto.get();
    }
    
    @GetMapping("players/")
    public Iterable<Player> getAll() {
        return repo.findAll();
    }
    
    @PutMapping("players/{id}")
    public void update(@PathVariable("id") String id, @RequestBody Player player) {
        player.setId(id);
        repo.save(player);
    }
}
