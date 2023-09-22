package de.yuga.spacebattle.backend.converter;

import de.yuga.spacebattle.backend.enums.EStarNation;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class EStarNationConverter implements AttributeConverter<Set<EStarNation>, String> {

    private static final String PIPE = "|";

    @Override
    public String convertToDatabaseColumn(@Nullable final Set<EStarNation> attribute) {
        if (attribute != null) {
            return attribute.stream()
                    .map(EStarNation::name)
                    .collect(Collectors.joining("|"));
        }
        return null;
    }

    @Override
    public Set<EStarNation> convertToEntityAttribute(@Nullable final String dbData) {
        final String[] result = EStringSetConverter.getStringsFromDBData(dbData);
        return Arrays.stream(result)
                .map(EStarNation::getRoleByName)
                .collect(Collectors.toSet());
    }
}
