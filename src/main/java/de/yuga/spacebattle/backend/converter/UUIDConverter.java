package de.yuga.spacebattle.backend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;
import java.util.UUID;

@Converter
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(@Nullable final UUID attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public UUID convertToEntityAttribute(@Nullable final String dbData) {
        return dbData != null ? UUID.fromString(dbData) : null;
    }
}
