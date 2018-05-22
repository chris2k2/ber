package de.cweyermann.ber.players.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;

import lombok.Data;

@Data
@DynamoDBTable(tableName = "Players")
public class Player {

    @DynamoDBTyped(DynamoDBAttributeType.S)
    public enum Sex {
        M, F, UNKNOWN
    }

    @DynamoDBHashKey
    private String id;

    private String name;

    private Integer ratingSingles;

    private Integer ratingDoubles;

    private Integer ratingMixed;

    private Sex sex;
}
