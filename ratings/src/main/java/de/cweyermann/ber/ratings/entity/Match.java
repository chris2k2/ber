package de.cweyermann.ber.ratings.entity;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;
import lombok.ToString;

@DynamoDBTable(tableName = "Matches")
@Data
@ToString
public class Match {

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    private String player1Id;

    private String player2Id;

    private String player3Id;

    private String player4Id;

    private List<Player> homePlayers;

    private List<Player> awayPlayers;

    private String league;

    private String hometeam;

    private String awayteam;

    private String discipline;

    private String result;

    private Date date;

}