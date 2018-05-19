package de.cweyermann.ber.matches.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
public class Match {

    public enum Discipline {
        MS, MD, WS, WD, MX
    }

    @Data
    @EqualsAndHashCode(of = { "id" })
    public static class Player {

        private String name;

        private String id;
    }

    private List<Player> homePlayers;

    private List<Player> awayPlayers;

    private String league;

    private String hometeam;

    private String awayteam;

    private Discipline discipline;

    private String result;

}
