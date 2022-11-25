package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.account.MessageThreadService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import de.yuga.spacebattle.rest.config.security.WebUserDetails;
import de.yuga.spacebattle.rest.dto.account.*;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
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
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@Tag(name = "AuthApi")
@RestController
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + AuthApi.ENDPOINT + "/")
public class AuthApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthApi.class);

    @Nonnull
    public static final String ENDPOINT = "auth";

    private static final String NAME_PLACEHOLDER = "NAME_PLACEHOLDER";
    private static final String WELCOME_MESSAGE = "Hello " + NAME_PLACEHOLDER + ",<br>" +
            "<br>" +
            "a happy welcome to the honorverse.<br>" +
            "<br>" +
            "I want to purpose that you have a look at the planet you conquered from the pirates.<br>" +
            "You can replay the battle at the journals section to see the glorious victory of your admirals and crews.<br>" +
            "<br>" +
            "In order to improve the conditions for your colonists you should have a look if you can build some houses or hospitals.<br>" +
            "But keep in mind, you can only house as many persons as you can support.<br>" +
            "<br>" +
            "If you noticed, the universe is a hostile place so it could be a good idea to build a shipyard and build ships to control your space.<br>" +
            "<br>" +
            "<br>" +
            "Sincerely and with the best wishes,<br>" +
            "Flashkid<br>";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final MessageThreadService messageThreadService;

    @Nonnull
    private final MasterOfTheUniverseService masterOfTheUniverseService;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nonnull
    private final AuthenticationManager authenticationManager;

    @Nonnull
    private final JwtTokenUtil jwtTokenUtil;

    @Nonnull
    private final TickService tickService;

    @Autowired
    public AuthApi(@Nonnull final AuthenticationManager authenticationManager,
                   @Nonnull final JwtTokenUtil jwtTokenUtil,
                   @Nonnull final UserService userService,
                   @Nonnull final ResearchService researchService,
                   @Nonnull final ColonizationService colonizationService,
                   @Nonnull final PlanetService planetService,
                   @Nonnull final MessageThreadService messageThreadService,
                   @Nonnull final MasterOfTheUniverseService masterOfTheUniverseService,
                   @Nonnull final TickService tickService) {
        Preconditions.checkNotNull(authenticationManager, "authenticationManager shouldn't be null!");
        Preconditions.checkNotNull(jwtTokenUtil, "jwtTokenUtil shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        Preconditions.checkNotNull(messageThreadService, "messageThreadService must not be empty");
        Preconditions.checkNotNull(masterOfTheUniverseService, "masterOfTheUniverseService must not be empty");
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");

        this.userService = userService;
        this.researchService = researchService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.colonizationService = colonizationService;
        this.planetService = planetService;
        this.messageThreadService = messageThreadService;
        this.masterOfTheUniverseService = masterOfTheUniverseService;
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

    @PostMapping(value = "/changePassword")
    @Operation(summary = "Triggers a password change.", operationId = "changePassword",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> changePassword(@RequestBody @Nonnull ChangePassword changePassword) {
        PreconditionWebHelper.checkNotNull(changePassword, "changePassword shouldn't be null!");

        final boolean changePasswordSuccessful = writeChangePasswordRequest(changePassword);
        return ResponseEntity.ok(changePasswordSuccessful);
    }

    private boolean writeChangePasswordRequest(@Nonnull final ChangePassword changePassword) {
        Preconditions.checkNotNull(changePassword, "changePassword shouldn't be null!");

        final WebUserDetails toModify = userService.findByUsername(changePassword.getUsername()).orElse(null);
        PreconditionWebHelper.checkNotNull(toModify, "toModify shouldn't be null!");
        final boolean eMailEquals = toModify.getUser().getEmail().equals(changePassword.geteMail());
        PreconditionWebHelper.checkArgument(!eMailEquals, "There was something wrong, guy!");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/changePassword/" + changePassword.getUsername()));
            writer.write(changePassword.geteMail());
            writer.close();
            return true;
        } catch (IOException e) {
            return false;
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
        if (!validate.isEmpty()) {
            throw new NotifyWebUserException("User could not be created", validate);
        }

        if (userService.existsEMail(userJson.getEmail())) {
            throw new NotifyWebUserException("eMail is already in use.");
        }
        if (userService.existsUsername(userJson.getUsername())) {
            throw new NotifyWebUserException("Username is already in use.");
        }
        final User entity = userJson.transform();
        final User saved = userService.save(entity);

        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        researchService.addResearch(saved, researchesWithoutPrecondition);

        Planet planet = colonizationService.findPlanetForNewUser();
        planet = planetService.save(planet);
        final Colonization colonization = new Colonization(saved, planet, ColonizationCostCalculator.getCrewRequirementForColonization(), 0);
        planet = colonizationService.colonizePlanet(colonization);
        tickService.operateInoperationals(planet);

        final Optional<WebUserDetails> sender = userService.findByUsername("Flashkid");
        sender.ifPresent(flash -> {
            final String replace = WELCOME_MESSAGE.replace(NAME_PLACEHOLDER, saved.getUsername());
            messageThreadService.createChatMessage(flash.getUser(), saved, replace);
        });

        masterOfTheUniverseService.createOpponentAndFightAsync(saved);
        return ResponseEntity.ok(new UserJson(saved));
    }

    @PostMapping("/checkUsername/{userName}")
    @Operation(summary = "Checks if a username already exists.", operationId = "checkUsername",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> checkUsername(@PathVariable("userName") @Nullable final String userName) {
        if (StringUtils.isBlank(userName) || userService.existsUsername(userName)) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/checkEmail/{eMail}")
    @Operation(summary = "Checks if a eMail already exists.", operationId = "checkEmail",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> checkEmail(@PathVariable("eMail") @Nullable final String eMail) {
        if (StringUtils.isBlank(eMail) || userService.existsEMail(eMail)) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }
}
