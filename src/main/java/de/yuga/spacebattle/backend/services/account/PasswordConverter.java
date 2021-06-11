package de.yuga.spacebattle.backend.services.account;

import de.yuga.spacebattle.NotifySBUserException;
import io.micrometer.core.instrument.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts the password to a hashed representation.
 * todo: hashing in frontend?
 */
@Converter
public class PasswordConverter implements AttributeConverter<String, String> {

    /**
     * Takes the given string and hashes it.
     *
     * @param password the string to hash
     * @return the hashed representation
     */
    @Nonnull
    @Override
    public String convertToDatabaseColumn(@Nullable final String password) {
        if (StringUtils.isBlank(password)) {
            throw new NotifySBUserException("Nothing to hash here.");
        }
        return new DigestUtils("SHA3-256").digestAsHex(password);
    }

    /**
     * Returns the pure database string.
     *
     * @param dbString the string in the database
     * @return the same string
     */
    @Nonnull
    @Override
    public String convertToEntityAttribute(@Nonnull final String dbString) {
        return dbString;
    }
}
