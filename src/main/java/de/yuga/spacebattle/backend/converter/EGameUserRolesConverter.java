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
        return Arrays.stream(dbData.split("\\|"))
                .map(EGameUserRole::getRoleByName)
                .collect(Collectors.toSet());
    }
}
