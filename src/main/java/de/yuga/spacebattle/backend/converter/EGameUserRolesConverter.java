package de.yuga.spacebattle.backend.converter;

import de.yuga.spacebattle.backend.enums.EGameUserRole;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class EGameUserRolesConverter implements AttributeConverter<Set<EGameUserRole>, String> {

    private static final String PIPE = "|";
    private static final String PIPE_REGEX = "\\" + PIPE;
    /**
     * Quickfix because spring returns the string comma separated from database -.-
     */
    private static final String COMMA_REGEX = ",";

    @Override
    public String convertToDatabaseColumn(@Nullable final Set<EGameUserRole> attribute) {
        if (attribute != null) {
            return attribute.stream()
                    .map(EGameUserRole::getName)
                    .collect(Collectors.joining("|"));
        }
        return null;
    }

    @Override
    public Set<EGameUserRole> convertToEntityAttribute(@Nullable final String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return new HashSet<>();
        }

        String[] result = {};
        if (dbData.contains(COMMA_REGEX)) {
            result = dbData.split(COMMA_REGEX);
        }
        if (dbData.contains(PIPE)) {
            result = dbData.split(PIPE_REGEX);
        }

        return Arrays.stream(result)
                .map(EGameUserRole::getRoleByName)
                .collect(Collectors.toSet());
    }
}
