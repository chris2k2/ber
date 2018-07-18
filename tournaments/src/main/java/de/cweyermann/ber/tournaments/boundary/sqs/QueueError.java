package de.cweyermann.ber.tournaments.boundary.sqs;

import java.util.Date;

import lombok.Data;

@Data
public class QueueError
{
    String stacktrace;
    
    String reason;
    
    Date time;
    
    String component;
}
