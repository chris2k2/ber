package de.cweyermann.ber.tournaments.boundary;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@DynamoDBTable(tableName = "Tournaments")
@Data
@ToString
public class DynmoDbTournament {

    @Data
    private static class TournamentId implements Serializable
    {
        private static final long serialVersionUID = 839270058975321930L;

        @DynamoDBHashKey
        private String id;
        
        @DynamoDBRangeKey
        private Date endDate;
    }
    
    @DynamoDBTyped(DynamoDBAttributeType.S)
    public enum ProccessingStatus {
        DONE, DOING, UNPROCESSED
    }

    @Id
    private TournamentId id;

    private String name;

    private ProccessingStatus status;

    private String source;
    
    private String type;

    @DynamoDBHashKey
    public String getId()
    {
        createId();
        return id.id;
    }
    
    public void setId(String id)
    {
        createId();
        this.id.id = id;
    }

    private void createId() {
        if(this.id == null)
        {
            this.id = new TournamentId();
        }
    }
    
    @DynamoDBRangeKey
    public Date getEndDate()
    {
        createId();
        return id.endDate;
    }
    
    public void setEndDate(Date date)
    {
        createId();
        id.endDate = date;
    }
    
}
