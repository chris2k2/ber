package de.cweyermann.ber.matches.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch;
import de.cweyermann.ber.matches.boundary.persistence.Repository;
import de.cweyermann.ber.matches.entity.Match;
import de.cweyermann.ber.matches.errors.UnknownMatchException;

@RestController
public class Endpoint {

    @Autowired
    private Repository repo;

    @Autowired
    private ModelMapper mapper;
    
    @GetMapping("matches/{id}")
    public Match getSingle(@PathVariable("id") String id) {
        Optional<DynamoDbMatch> dto = repo.findById(id);
        if (!dto.isPresent()) {
            throw new UnknownMatchException(id);
        }

        return mapper.map(dto.get(), Match.class);
    }

    @GetMapping("matches")
    public List<Match> getAllForPlayer(
            @RequestParam(name = "playerid", required = false) String id) {
        List<DynamoDbMatch> matches = new ArrayList<>();
        if (StringUtils.isEmpty(id)) {
            repo.findAll().forEach(matches::add);
        } else {
            matches = repo.findByAnyPlayerId(id);
        }
    
        return matches.stream().map(m -> mapper.map(m, Match.class)).collect(Collectors.toList());
    }

    @GetMapping("ping")
    public String ping() {
        return "pong";
    }
}
