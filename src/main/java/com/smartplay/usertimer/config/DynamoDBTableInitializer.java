package com.smartplay.usertimer.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

import jakarta.annotation.PostConstruct;

@Configuration
public class DynamoDBTableInitializer {

    private final AmazonDynamoDB amazonDynamoDB;

    public DynamoDBTableInitializer(@Autowired AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @PostConstruct
    public void init() {
        checkAndCreateTable("TestTable", "id");
        checkAndCreateTable("UserTimer","id");
        // checkAndCreateTable("UserTimer","id");
        // checkAndCreateTable("SmartPlayGamePoint","requestId");
    }

    private void checkAndCreateTable(String tableName, String keyName) {
        if (!doesTableExist(tableName)) {
            createTable(tableName, keyName);
        }
    }

    private boolean doesTableExist(String tableName) {
        try {
            TableDescription tableDescription = amazonDynamoDB.describeTable(tableName).getTable();
            return tableDescription.getTableStatus().equals(TableStatus.ACTIVE.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private void createTable(String tableName, String keyName) {
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(new KeySchemaElement(keyName, KeyType.HASH))
                .withAttributeDefinitions(new com.amazonaws.services.dynamodbv2.model.AttributeDefinition(keyName, com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        amazonDynamoDB.createTable(request);
    }
}