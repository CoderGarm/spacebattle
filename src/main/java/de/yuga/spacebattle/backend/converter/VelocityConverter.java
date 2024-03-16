package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Velocity;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class VelocityConverter implements AttributeConverter<Velocity, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Velocity attribute) {
        return attribute != null ? attribute.asString() : null;
    }

    @Override
    public Velocity convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Velocity.getFromString(dbData) : null;
    }

}
