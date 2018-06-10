package de.cweyermann.ber.tournaments.entity;

import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Tournament {

    public enum ProccessingStatus {
        DONE, DOING, UNPROCESSED
    }

    private String id;

    private String name;

    private ProccessingStatus status;

    private String source;

    private Date endDate;
    
    private String type;
}
