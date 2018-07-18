package de.cweyermann.ber.playerratings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class CalcRatingsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalcRatingsApplication.class, args);
    }
}
