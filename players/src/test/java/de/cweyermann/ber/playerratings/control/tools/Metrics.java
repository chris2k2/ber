package de.cweyermann.ber.playerratings.control.tools;

import de.cweyermann.ber.playerratings.entity.Match;
import lombok.Getter;

@Getter
public class Metrics {

    private int matches = 0;

    private int favWonIn3 = 0;

    private int favWonIn2 = 0;

    private int favLostIn2 = 0;

    private int favLostIn3 = 0;

    private int ratingChange = 0;

    private int ratingChangeAbsolut = 0;

    public void addMatch(Match m) {

    }

}
