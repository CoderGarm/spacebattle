package de.yuga.spacebattle.backend.converter;

import de.yuga.spacebattle.backend.enums.events.EGameEvent;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class EGameEventConverter implements AttributeConverter<Set<EGameEvent>, String> {

    private static final String PIPE = "|";

    @Override
    public String convertToDatabaseColumn(@Nullable final Set<EGameEvent> attribute) {
        if (attribute != null) {
            return attribute.stream()
                    .map(EGameEvent::getName)
                    .collect(Collectors.joining("|"));
        }
        return null;
    }

    @Override
    public Set<EGameEvent> convertToEntityAttribute(@Nullable final String dbData) {
        final String[] result = EStringSetConverter.getStringsFromDBData(dbData);
        return Arrays.stream(result)
                .map(EGameEvent::getRoleByName)
                .collect(Collectors.toSet());
    }
}
