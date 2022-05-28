package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import de.yuga.spacebattle.rest.config.security.WebUserDetails;
import de.yuga.spacebattle.rest.dto.account.AuthRequest;
import de.yuga.spacebattle.rest.dto.account.JWT;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.account.UserReq;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@Tag(name = "AuthApi")
@RestController
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + AuthApi.ENDPOINT + "/")
public class AuthApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthApi.class);

    @Nonnull
    public static final String ENDPOINT = "auth";

    @Nonnull
    private final UserService service;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nonnull
    private final AuthenticationManager authenticationManager;

    @Nonnull
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public AuthApi(@Nonnull final AuthenticationManager authenticationManager,
                   @Nonnull final JwtTokenUtil jwtTokenUtil,
                   @Nonnull final UserService userService) {
        Preconditions.checkNotNull(authenticationManager, "authenticationManager shouldn't be null!");
        Preconditions.checkNotNull(jwtTokenUtil, "jwtTokenUtil shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.service = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Does a login", operationId = "login",
            description = "Takes parameters and tries to create a valid login from it.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JWT.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            final Set<ConstraintViolation<AuthRequest>> validate = validator.validate(request);
            if (!validate.isEmpty()) {
                LOGGER.error("Auth request not valid - check it!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            final Authentication authenticate = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            final WebUserDetails webUser = (WebUserDetails) authenticate.getPrincipal();
            final User user = webUser.getUser();
            final String accessToken = jwtTokenUtil.generateAccessToken(user);
            final String refreshToken = jwtTokenUtil.generateRefreshToken(user);

            if (!jwtTokenUtil.validate(accessToken)) {
                LOGGER.error("Access token not valid by generation - check it!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            if (!jwtTokenUtil.validate(refreshToken)) {
                LOGGER.error("Refresh token not valid by generation - check it!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, accessToken).body(new JWT(user, accessToken, refreshToken));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping(value = "/refresh")
    @Operation(summary = "Does a refresh of the access token", operationId = "refresh",
            description = "Takes the refresh token and tries to create a valid login from it.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JWT.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> refresh(@RequestHeader("refresh-token") String refreshToken) {

        if (!jwtTokenUtil.validate(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final User user = jwtTokenUtil.getUserByRefreshToken(refreshToken);
        if (user != null) {
            final String accessToken = jwtTokenUtil.generateAccessToken(user);
            final String newRefreshToken = jwtTokenUtil.generateRefreshToken(user);
            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, accessToken).body(new JWT(user, accessToken, newRefreshToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Creates a single user", operationId = "createUser",
            description = "Creates and returns a user which is now registered in the system",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserReq.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserJson.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> create(@RequestBody @Nonnull final UserReq userJson) {
        Preconditions.checkNotNull(userJson, "userJson shouldn't be null!");

        final Set<ConstraintViolation<UserReq>> validate = validator.validate(userJson);
        if (validate.isEmpty()) {
            if (service.existsEMail(userJson.getEmail())) {
                throw new NotifyWebUserException("eMail is already in use.");
            }
            if (service.existsUsername(userJson.getUsername())) {
                throw new NotifyWebUserException("Username is already in use.");
            }
            final User entity = userJson.transform();
            final User saved = service.save(entity);
            return ResponseEntity.ok(new UserJson(saved));
        }
        throw new NotifyWebUserException("User could not be created", validate);
    }
}
