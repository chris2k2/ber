package de.cweyermann.ber.tournaments.boundary.persistence;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.DynamoDbTournament.TournamentId;

@EnableScan
public interface Repository
        extends CrudRepository<DynamoDbTournament, DynamoDbTournament.TournamentId> {

    List<DynamoDbTournament> findByIdIn(List<String> ids);
    
    List<DynamoDbTournament> findAll();

    List<DynamoDbTournament> findAllBySource(String source);

    List<DynamoDbTournament> findByStatus(ProccessingStatus status);

    List<DynamoDbTournament> findByStatusAndSource(ProccessingStatus status, String source);

    default Optional<DynamoDbTournament> findFirstBySourceAndStatusOrderByEndDateDesc(String source,
            ProccessingStatus status) {
        return getLatest(findByStatusAndSource(status, source));
    }

    default Optional<DynamoDbTournament> findFirstBySourceOrderByEndDateDesc(String source) {
        return getLatest(findAllBySource(source));
    }

    default Optional<DynamoDbTournament> findFirstByStatusOrderByEndDateDesc(
            ProccessingStatus status) {
        return getLatest(findByStatus(status));
    }

    default Optional<DynamoDbTournament> findFirstOrderByEndDateDesc() {
        return getLatest(findAll());
    }

    default Optional<DynamoDbTournament> getLatest(List<DynamoDbTournament> tournaments) {
        return tournaments.stream()
                .filter(x -> x.getEndDate().before(new Date()))
                .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                .findFirst();
    }

    default List<String> findDistinctBySource() {
        return findAll().stream().map(t -> t.getSource()).distinct().collect(Collectors.toList());
    }

    default Optional<DynamoDbTournament> findById(String id, Date endDate) {
        DynamoDbTournament.TournamentId tid = new TournamentId();
        tid.setId(id);
        tid.setEndDate(endDate);

        return findById(tid);
    }
}
