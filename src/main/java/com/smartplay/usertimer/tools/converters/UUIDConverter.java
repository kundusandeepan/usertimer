package com.smartplay.usertimer.tools.converters;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class UUIDConverter implements DynamoDBTypeConverter<String, UUID> {

    @Override
    public String convert(UUID object) {
        return object.toString();
    }

    @Override
    public UUID unconvert(String object) {
        return UUID.fromString(object);
    }
}