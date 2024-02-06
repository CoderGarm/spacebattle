package de.yuga.spacebattle.backend.converter;


import de.yuga.spacebattle.backend.dto.physics.Distance;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DistanceConverter implements AttributeConverter<Distance, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final Distance attribute) {
        return attribute != null ? attribute.toDatabaseString() : null;
    }

    @Override
    public Distance convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? Distance.valueOf(dbData) : null;
    }

}
