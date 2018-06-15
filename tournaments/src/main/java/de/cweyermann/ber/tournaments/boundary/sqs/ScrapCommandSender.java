package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament;
import de.cweyermann.ber.tournaments.boundary.persistence.DynmoDbTournament.ProccessingStatus;
import de.cweyermann.ber.tournaments.boundary.persistence.Repository;
import de.cweyermann.ber.tournaments.entity.Tournament;
import io.vavr.Tuple2;

@Component
public class ScrapCommandSender {

    private static final ProccessingStatus DONE = ProccessingStatus.DONE;

    @Autowired
    protected Repository repo;

    @SqsListener(value = "NewTournament", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    @SendTo("Scrap")
    public List<Tournament> filter(List<Tournament> tournaments) {

        io.vavr.collection.List<Tournament> vavrTournaments = io.vavr.collection.List
                .ofAll(tournaments);

        return vavrTournaments.map(t -> t.getId()) // [1 ,2]
                .map(id -> repo.findById(id)) // [db1, db2]
                .zip(vavrTournaments) // [(db1, t1), (db2, t2)]
                .filter(x -> (!db(x).isPresent() || dbStatus(x) != DONE)) // [(db1, t1)]
                .map(tuple -> tuple._2) // [t1]
                .asJava();
    }

    private ProccessingStatus dbStatus(Tuple2<Optional<DynmoDbTournament>, Tournament> tuple) {
        return db(tuple).get().getStatus();
    }

    private Optional<DynmoDbTournament> db(Tuple2<Optional<DynmoDbTournament>, Tournament> tuple) {
        return tuple._1();
    }
}
