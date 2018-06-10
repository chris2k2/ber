package de.cweyermann.ber.tournaments.boundary;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.tournaments.boundary.DynmoDbTournament.ProccessingStatus;

@EnableScan
public interface Repository extends CrudRepository<DynmoDbTournament, String> {

    List<DynmoDbTournament> findAll();

    List<DynmoDbTournament> findAllBySource(String source);

    List<DynmoDbTournament> findByStatus(ProccessingStatus status);

    List<DynmoDbTournament> findByStatusAndSource(ProccessingStatus status, String source);

    default Optional<DynmoDbTournament> findFirstBySourceAndStatusOrderByEndDateDesc(String source,
            ProccessingStatus status) {
        return getLatest(findByStatusAndSource(status, source));
    }

    default Optional<DynmoDbTournament> findFirstBySourceOrderByEndDateDesc(String source) {
        return getLatest(findAllBySource(source));
    }

    default Optional<DynmoDbTournament> findFirstByStatusOrderByEndDateDesc(ProccessingStatus status) {
        return getLatest(findByStatus(status));
    }
    
    default Optional<DynmoDbTournament> findFirstOrderByEndDateDesc() {
        return getLatest(findAll());
    }

    default Optional<DynmoDbTournament> getLatest(List<DynmoDbTournament> tournaments) {
        return tournaments.stream()
                .filter(x -> x.getEndDate().after(new Date()))
                .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                .findFirst();
    }

}
