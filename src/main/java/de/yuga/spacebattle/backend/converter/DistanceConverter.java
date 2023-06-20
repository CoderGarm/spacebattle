package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Distance;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;

@Converter
public class DistanceConverter implements AttributeConverter<Distance, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Distance attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Distance convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Distance.valueOf(dbData) : null;
    }

}
