package de.cweyermann.ber.matches.boundary.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;

@Data
@DynamoDBDocument
public class DynamoDbPlayer {

    private String name;

    private String id;
    
    private Integer rating;
}