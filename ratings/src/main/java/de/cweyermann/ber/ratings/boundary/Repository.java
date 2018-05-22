package de.cweyermann.ber.ratings.boundary;

import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.ratings.entity.Match;
import de.cweyermann.ber.ratings.entity.Match.Status;

@EnableScan
public interface Repository extends CrudRepository<Match, String> {

    List<Match> findByProcessStatus(Status status);

    default List<Match> getAllUnratedMatches() {
        return findByProcessStatus(Status.UNRATED);
    }
}
