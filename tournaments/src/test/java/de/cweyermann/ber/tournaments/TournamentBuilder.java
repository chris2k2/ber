package de.cweyermann.ber.tournaments;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import de.cweyermann.ber.tournaments.entity.Tournament;
import de.cweyermann.ber.tournaments.entity.Tournament.ProccessingStatus;

public class TournamentBuilder {
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");
    
    public static Tournament tournament(String id, String endDate, String source,
            ProccessingStatus status) throws ParseException {
        Tournament t1 = new Tournament();
        t1.setId(id);
        t1.setEndDate(FORMAT.parse(endDate));
        t1.setStatus(status);
        t1.setSource(source);

        return t1;
    }
}
