package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Acceleration;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AccelerationConverter implements AttributeConverter<Acceleration, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Acceleration attribute) {
        return attribute != null ? attribute.asString() : null;
    }

    @Override
    public Acceleration convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Acceleration.getFromString(dbData) : null;
    }
}
