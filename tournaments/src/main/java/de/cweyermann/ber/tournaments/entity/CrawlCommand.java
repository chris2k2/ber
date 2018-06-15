package de.cweyermann.ber.tournaments.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.ToString;

@ToString
public class CrawlCommand {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    
    private Date lastEndDate;
    
    private String niceLastEndDate;

    private String source;
    
    public Date getLastEndDate() {
        return lastEndDate;
    }

    public void setLastEndDate(Date lastEndDate) {
        this.lastEndDate = lastEndDate;
        this.niceLastEndDate = dateFormat.format(lastEndDate);
    }

    public String getNiceLastEndDate() {
        return niceLastEndDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
}
