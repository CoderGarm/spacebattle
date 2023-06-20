package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Mass;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;

@Converter
public class MassConverter implements AttributeConverter<Mass, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Mass attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Mass convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Mass.valueOf(dbData) : null;
    }

}
