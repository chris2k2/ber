package de.cweyermann.ber.tournaments.boundary.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;
import lombok.extern.log4j.Log4j2;

/**
 * Supported use cases:
 * <ul>
 * <li>Get latest Tournament of your source to know when to start crawling</li>
 * <li>Get all unprocessed tournaments to scrap matches</li>
 * <li>Update tournament ot set it to DONE/DOING</li>
 * <li>Add new tournament</li>
 * </ul>
 * 
 * @author chris
 *
 */
@Log4j2
@RestController
public class Endpoint {

    @Autowired
    private Repository repo;

    @Autowired
    private ModelMapper mapper;
    
    @GetMapping(path = "tournaments", produces = "application/json")
    public List<Tournament> getAll(
            @RequestParam(name = "status", required = false) ProccessingStatus status,
            @RequestParam(name = "latest", required = false) Boolean latest,
            @RequestParam(name = "source", required = false) String source) {
        List<DynmoDbTournament> result = Collections.emptyList();
        if (status == null && source == null && latest == null) {
            result = repo.findAll();
        } else if (status != null && source == null && latest == null) {
            result = repo.findByStatus(status);
        } else if (status == null && source != null && latest == null) {
            result = repo.findAllBySource(source);
        } else if (status != null && source != null && latest == null) {
            result = repo.findByStatusAndSource(status, source);
        } else if (status == null && source == null && latest != null) {
            result = toList(repo.findFirstOrderByEndDateDesc());
        } else if (status != null && source == null && latest != null) {
            result = toList(repo.findFirstByStatusOrderByEndDateDesc(status));
        } else if (status == null && source != null && latest != null) {
            result = toList(repo.findFirstBySourceOrderByEndDateDesc(source));
        } else if (status != null && source != null && latest != null) {
            result = toList(repo.findFirstBySourceAndStatusOrderByEndDateDesc(source, status));
        }

        List<Tournament> tournaments = new ArrayList<>();
        for (DynmoDbTournament dynamoT : result) {
            tournaments.add(mapper.map(dynamoT, Tournament.class));
        }

        return tournaments;
    }

    private List<DynmoDbTournament> toList(Optional<DynmoDbTournament> opt) {
        List<DynmoDbTournament> res = new ArrayList<>();
        if (opt.isPresent()) {
            res = Arrays.asList(opt.get());
        }

        return res;
    }

    @PostMapping(path = "tournaments", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTournaments(@RequestBody List<Tournament> newTournament) {
        List<DynmoDbTournament> tournaments = new ArrayList<>();

        for (Tournament t : newTournament) {
            log.debug("Checking: " + t);

            Optional<DynmoDbTournament> oldOne = repo.findById(t.getId());

            if (!oldOne.isPresent() || oldOne.get().getStatus() != ProccessingStatus.DONE) {
                tournaments.add(mapper.map(t, DynmoDbTournament.class));
            }
        }

        if (!tournaments.isEmpty()) {
            repo.saveAll(tournaments);
        }

    }

    @PutMapping(path = "tournaments/{id}", consumes = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void update(@RequestBody Tournament updatedTournament, @PathVariable("id") String id) {
        updatedTournament.setId(id);
        repo.save(mapper.map(updatedTournament, DynmoDbTournament.class));
    }
}
