package de.cweyermann.ber.tournaments.boundary;

import java.util.List;
import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.tournaments.entity.Tournament;
import de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus;

@EnableScan
public interface Repository extends CrudRepository<Tournament, String> {

    List<Tournament> findAll();

    List<Tournament> findAllBySource(String source);

    List<Tournament> findByStatus(ProccessingStatus status);

    List<Tournament> findByStatusAndSource(ProccessingStatus status, String source);

    default Optional<Tournament> findFirstBySourceAndStatusOrderByEndDateDesc(String source,
            ProccessingStatus status) {
        return getLatest(findByStatusAndSource(status, source));
    }

    default Optional<Tournament> findFirstBySourceOrderByEndDateDesc(String source) {
        return getLatest(findAllBySource(source));
    }

    default Optional<Tournament> findFirstByStatusOrderByEndDateDesc(ProccessingStatus status) {
        return getLatest(findByStatus(status));
    }
    
    default Optional<Tournament> findFirstOrderByEndDateDesc() {
        return getLatest(findAll());
    }

    default Optional<Tournament> getLatest(List<Tournament> tournaments) {
        return tournaments.stream()
                .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                .findFirst();
    }

}
