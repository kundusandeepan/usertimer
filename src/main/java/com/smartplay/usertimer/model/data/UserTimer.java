package com.smartplay.usertimer.model.data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.smartplay.usertimer.tools.converters.LocalDateTimeConverter;
import com.smartplay.usertimer.tools.converters.UUIDConverter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@NotNull
@EqualsAndHashCode(callSuper=false)
@DynamoDBTable(tableName = "UserTimer")
public class UserTimer implements Serializable {

    private static final long serialVersionUID = 1L;

    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    private UUID id;
    
    private String lpaId;

    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime startTime;

    /**
     * The duration of the user timer in seconds.
     */
    private long duration;

    @DynamoDBTypeConverted(converter = LocalDateTimeConverter.class)
    private LocalDateTime endTime;
    // public LocalDateTime getEndTime() {
    //     return this.startTime.plusSeconds(this.duration);
    // }
}
