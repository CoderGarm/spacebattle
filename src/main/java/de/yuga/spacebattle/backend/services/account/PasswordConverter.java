package de.yuga.spacebattle.backend.services.account;

import de.yuga.spacebattle.NotifySBUserException;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PasswordConverter implements AttributeConverter<String, String> {

    private static final String SEPARATOR = ", ";

    @Nonnull
    @Override
    public String convertToDatabaseColumn(@Nullable final String password) {
        if (StringUtils.isBlank(password)) {
            throw new NotifySBUserException("Nothing to hash here.");
        }
        return new DigestUtils("SHA3-256").digestAsHex(password);
    }

    @Nonnull
    @Override
    public String convertToEntityAttribute(@Nonnull final String dbString) {
        return dbString;
    }
}
