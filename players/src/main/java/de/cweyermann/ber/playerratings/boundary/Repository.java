package de.cweyermann.ber.playerratings.boundary;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

import de.cweyermann.ber.playerratings.entity.Player;

@EnableScan
public interface Repository extends CrudRepository<Player, String> {

}
