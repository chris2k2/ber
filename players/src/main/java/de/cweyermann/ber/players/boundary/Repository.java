package de.cweyermann.ber.players.boundary;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.players.entity.Player;

@EnableScan
public interface Repository extends CrudRepository<Player, String> {

}
