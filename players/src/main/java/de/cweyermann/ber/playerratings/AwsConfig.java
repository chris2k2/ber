package de.cweyermann.ber.playerratings;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

import de.cweyermann.ber.playerratings.control.Elo;
import de.cweyermann.ber.playerratings.control.EloStrategies;

@Configuration
@EnableAutoConfiguration
@EnableDynamoDBRepositories(basePackages = "de.cweyermann.ber")
public class AwsConfig {

    @Value("${amazon.aws.dynamodb.region}")
    private String signingRegion;

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AWSStaticCredentialsProvider credentials = new AWSStaticCredentialsProvider(
                amazonAWSCredentials());

        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(signingRegion)
                .build();

        return amazonDynamoDB;
    }

    
    @Bean
    public AWSCredentials amazonAWSCredentials() {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }
    
    @Bean
    public AmazonSQSAsync amazonSQSClient() {
        AWSStaticCredentialsProvider credentials = new AWSStaticCredentialsProvider(
                amazonAWSCredentials());

        AmazonSQSAsync sqs = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(credentials)
                .withRegion(signingRegion)
                .build();

        return sqs;
        
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate() {
         return new QueueMessagingTemplate(amazonSQSClient());
    }
    
    @Bean
    public QueueMessageHandler queueMessageHandler(final AmazonSQSAsync amazonSqs) {
        final QueueMessageHandlerFactory queueMsgHandlerFactory = new QueueMessageHandlerFactory();
        queueMsgHandlerFactory.setAmazonSqs(amazonSqs);

        final QueueMessageHandler queueMessageHandler = queueMsgHandlerFactory.createQueueMessageHandler();

        return queueMessageHandler;
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(final AmazonSQSAsync amazonSqs) {
        final SimpleMessageListenerContainerFactory msgListenerContainerFactory = new SimpleMessageListenerContainerFactory();
        msgListenerContainerFactory.setAmazonSqs(amazonSqs);
        msgListenerContainerFactory.setMaxNumberOfMessages(5);
        msgListenerContainerFactory.setWaitTimeOut(20);

        return msgListenerContainerFactory;
    }


    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(final QueueMessageHandler messageHandler, final SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory) {
        final SimpleMessageListenerContainer msgListenerContainer = simpleMessageListenerContainerFactory.createSimpleMessageListenerContainer();
        msgListenerContainer.setMessageHandler(messageHandler);

        return msgListenerContainer;
    }

    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory(final AmazonSQSAsync amazonSqs) {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        factory.setAmazonSqs(amazonSqs);

        return factory;
    }
    

    @Bean
    public Elo defaultElo() {
        return new Elo(EloStrategies.SIMPLE_WIN_LOOSE, EloStrategies.EVERYONE_1000,
                EloStrategies.K_CONST8, EloStrategies.AVERAGE);
    }
}