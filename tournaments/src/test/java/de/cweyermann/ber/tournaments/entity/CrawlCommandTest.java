package de.cweyermann.ber.tournaments.entity;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class CrawlCommandTest {

    @Test
    public void setDate_niceDateAlsoSet() {
        CrawlCommand crawlCommand = new CrawlCommand();
        crawlCommand.setLastEndDate(new Date(0));

        assertEquals("19700101", crawlCommand.getNiceLastEndDate());
    }
}
