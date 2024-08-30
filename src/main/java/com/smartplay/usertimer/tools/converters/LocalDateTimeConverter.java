package com.smartplay.usertimer.tools.converters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class LocalDateTimeConverter implements DynamoDBTypeConverter<String, LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    @Override
    public String convert(LocalDateTime object) {
        return object.format(FORMATTER);
    }

    @Override
    public LocalDateTime unconvert(String object) {
        return LocalDateTime.parse(object, FORMATTER);
    }
}

