package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifyUserException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts the password to a hashed representation.
 */
@Converter
public class PasswordConverter implements AttributeConverter<String, String>, PasswordEncoder {

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
            throw new NotifyUserException("Nothing to hash here.");
        }
        return new DigestUtils("SHA3-512").digestAsHex(password);
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

    @Override
    public String encode(@Nonnull final CharSequence rawPassword) {
        Preconditions.checkNotNull(rawPassword, "rawPassword shouldn't be null!");

        return convertToDatabaseColumn(rawPassword.toString());
    }

    @Override
    public boolean matches(@Nonnull final CharSequence rawPassword, @Nonnull final String encodedPassword) {
        Preconditions.checkNotNull(rawPassword, "rawPassword shouldn't be null!");
        Preconditions.checkNotNull(encodedPassword, "encodedPassword shouldn't be null!");

        final String newlyEncodedPassword = convertToDatabaseColumn(rawPassword.toString());
        return newlyEncodedPassword.equals(encodedPassword);
    }
}
