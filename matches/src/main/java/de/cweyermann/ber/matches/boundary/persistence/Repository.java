package de.cweyermann.ber.matches.boundary.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.matches.boundary.persistence.DynamoDbMatch.Status;

@EnableScan
public interface Repository extends CrudRepository<DynamoDbMatch, String> {

    public List<DynamoDbMatch> findByPlayer1Id(String id);

    public List<DynamoDbMatch> findByPlayer2Id(String id);

    public List<DynamoDbMatch> findByPlayer3Id(String id);

    public List<DynamoDbMatch> findByPlayer4Id(String id);

    default public List<DynamoDbMatch> findByAnyPlayerId(String id) {
        List<DynamoDbMatch> matches = new ArrayList<>();
        matches.addAll(findByPlayer1Id(id));
        matches.addAll(findByPlayer2Id(id));
        matches.addAll(findByPlayer3Id(id));
        matches.addAll(findByPlayer4Id(id));

        return matches;
    }

    List<DynamoDbMatch> findByIdIn(List<String> ids);

    default List<DynamoDbMatch> getAllUnratedMatches() {
        return findByProcessStatus(Status.UNRATED);
    }

    public List<DynamoDbMatch> findByProcessStatus(Status status);
}