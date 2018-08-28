package de.cweyermann.ber.playerratings.control.tools;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import de.cweyermann.ber.playerratings.boundary.Repository;

@Configuration
@EnableAutoConfiguration
@EnableDynamoDBRepositories(basePackageClasses= {Repository.class, MatchRepo.class})
public class LocalDynamoConfig extends de.cweyermann.ber.matches.AwsConfig {

}