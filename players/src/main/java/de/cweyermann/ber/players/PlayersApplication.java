package de.cweyermann.ber.players;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;


@SpringBootApplication
@EnableEurekaClient
public class PlayersApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlayersApplication.class, args);
    }
}
