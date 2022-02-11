package de.yuga.spacebattle.rest.config.security;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.services.account.PasswordConverter;
import de.yuga.spacebattle.backend.services.account.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.DefaultJwtSignatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtTokenUtil {

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Nonnull
    public static final SignatureAlgorithm HS_256 = SignatureAlgorithm.HS256;

    @Nonnull
    public static final String SECRET_KEY_PLAINTEXT = "iRoh6iezajaeru";

    @Nonnull
    public final String SECRET_KEY_BASE64_ENCODED;

    @Nonnull
    public static final String USER_ID_CLAIM = "userID";

    @Nonnull
    public static final String PASSWORD_CLAIM = "password";

    @Nonnull
    public static final String ROLE_CLAIM = "role";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final PasswordConverter passwordConverter = new PasswordConverter();

    @Autowired
    public JwtTokenUtil(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.userService = userService;
        SECRET_KEY_BASE64_ENCODED = Base64.getEncoder().encodeToString(SECRET_KEY_PLAINTEXT.getBytes());
    }

    public boolean validate(@Nonnull final String token) {
        Preconditions.checkNotNull(token, "token shouldn't be null!");

        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_PLAINTEXT.getBytes(), HS_256.getJcaName());

        String[] chunks = token.split("\\.");
        String tokenWithoutSignature = chunks[0] + "." + chunks[1];
        String signature = chunks[2];

        DefaultJwtSignatureValidator validator = new DefaultJwtSignatureValidator(HS_256, secretKeySpec);

        if (!validator.isValid(tokenWithoutSignature, signature)) {
            LOGGER.info("Could not verify JWT token integrity!");
            return false;
        }
        return true;
    }

    /**
     * Checks if the refresh token is valid and returns the identified user.<br>
     * If nothing is in return than no user could identified.
     *
     * @param refreshToken the refresh token
     * @return the user which is identified by the refresh token
     */
    @Nullable
    public User getUserByRefreshToken(@Nonnull final String refreshToken) {
        Preconditions.checkNotNull(refreshToken, "refreshToken shouldn't be null!");

        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY_BASE64_ENCODED)
                    .parseClaimsJws(refreshToken)
                    .getBody();
            final Integer userID = claims.get(USER_ID_CLAIM, Integer.class);
            final String hashedPassword = claims.get(PASSWORD_CLAIM, String.class);
            final User user = userService.find(userID);
            if (user != null && user.getPassword().equals(hashedPassword) && user.getUsername().equals(claims.getSubject())) {
                return user;
            }
        } catch (final Exception e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    @Nullable
    public String getUsernameFromAccessToken(@Nonnull final String token) {
        Preconditions.checkNotNull(token, "token shouldn't be null!");

        try {
            final Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY_BASE64_ENCODED)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (final Exception e) {
            LOGGER.info(e.getMessage());
            return null;
        }
    }

    public String generateAccessToken(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put(USER_ID_CLAIM, user.getId());
        claims.put(ROLE_CLAIM, EWebUserRole.USER);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate(5))
                .signWith(HS_256, SECRET_KEY_BASE64_ENCODED)
                .compact();
    }

    public String generateRefreshToken(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        Claims claims = Jwts.claims().setSubject(user.getUsername());
        claims.put(USER_ID_CLAIM, user.getId());
        claims.put(PASSWORD_CLAIM, user.getPassword());

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate(60))
                .signWith(HS_256, SECRET_KEY_BASE64_ENCODED)
                .compact();
    }

    private Date generateExpirationDate(final double minutes) {
        return new Date(System.currentTimeMillis() + (int) (minutes * 60 * 1000));
    }


}
