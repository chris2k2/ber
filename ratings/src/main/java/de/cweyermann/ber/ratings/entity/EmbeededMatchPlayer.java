package de.cweyermann.ber.ratings.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;

@Data
@DynamoDBDocument
public class EmbeededMatchPlayer {

    private String name;

    private String id;
    
    private Integer rating;
    
    private Integer afterRating;
}