package com.smartplay.usertimer.model.data;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DynamoDBTable(tableName = "TestTable")
public class TestEntity {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    private String name;
}