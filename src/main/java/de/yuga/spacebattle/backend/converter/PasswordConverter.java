package de.yuga.spacebattle.backend.converter;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;

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
    public String convertToDatabaseColumn(@Nonnull final String password) {
        if (StringUtils.isBlank(password)) {
            throw new NotifyWebUserException("Nothing to hash here.");
        }
        if (password.length() > 30) {
            // do not change a hashed password
            return password;
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
