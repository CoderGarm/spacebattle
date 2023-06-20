package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Time;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;

@Converter
public class TimeConverter implements AttributeConverter<Time, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Time attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Time convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Time.valueOf(dbData) : null;
    }

}
