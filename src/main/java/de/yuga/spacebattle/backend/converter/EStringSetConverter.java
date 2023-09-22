package de.yuga.spacebattle.backend.converter;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class EStringSetConverter implements AttributeConverter<Set<String>, String> {

    private static final String PIPE = "|";
    private static final String PIPE_REGEX = "\\" + PIPE;
    /**
     * Quickfix because spring returns the string comma separated from database -.-
     */
    private static final String COMMA_REGEX = ",";

    @Override
    public String convertToDatabaseColumn(@Nullable final Set<String> attribute) {
        if (attribute != null) {
            return String.join("|", attribute);
        }
        return null;
    }

    @Override
    public Set<String> convertToEntityAttribute(@Nullable final String dbData) {
        final String[] result = EStringSetConverter.getStringsFromDBData(dbData);
        return Arrays.stream(result).collect(Collectors.toSet());
    }

    @Nonnull
    public static String[] getStringsFromDBData(@Nullable final String dbData) {
        final String[] result;
        if (StringUtils.isBlank(dbData)) {
            result = new String[]{};
        } else if (dbData.contains(COMMA_REGEX)) {
            result = dbData.split(COMMA_REGEX);
        } else if (dbData.contains(PIPE)) {
            result = dbData.split(PIPE_REGEX);
        } else {
            result = new String[]{dbData};
        }
        return result;
    }
}
