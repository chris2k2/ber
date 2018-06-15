package de.cweyermann.ber.ratings.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(of = { "homePlayers", "awayPlayers", "league", "result" })
public class Match {

    public enum Status
    {
        RATED, DOING, UNRATED
    }
    
    @Data
    @EqualsAndHashCode(of = { "id" })
    public static class Player {

        private String name;

        private String id;
        
        private Integer oldRating;
        
        private Integer newRating;
    }

    private List<Player> homePlayers;

    private List<Player> awayPlayers;

    private String league;

    private String hometeam;

    private String awayteam;

    private String discipline;

    private String result;
    
    private int leagueDepth;
    
    private Status processStatus = Status.UNRATED;

    public String getMatchId()
    {
        return hashCode() + "";
    }
}
