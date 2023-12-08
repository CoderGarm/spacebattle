package de.yuga.spacebattle.rest.api.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.colonization.ColonizationCostCalculator;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.services.MailService;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import de.yuga.spacebattle.backend.services.account.ChatService;
import de.yuga.spacebattle.backend.services.account.NonPlayerCharacterService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.constructables.OperationalService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.tick.mission.HeatMapService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static de.yuga.spacebattle.backend.services.MasterOfTheUniverseService.DEFEATED_OPPONENT;
import static de.yuga.spacebattle.rest.api.EndpointDefinition.PUBLIC_BASE_ENDPOINT;

@RestController
@Tag(name = "AuthApi")
@RequestMapping(value = "/" + PUBLIC_BASE_ENDPOINT + "/" + AuthApi.ENDPOINT + "/")
public class AuthApi {

    private static SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthApi.class);

    @Nonnull
    public static final String ENDPOINT = "auth";

    private static final String NAME_PLACEHOLDER = "NAME_PLACEHOLDER";
    private static final String WELCOME_MESSAGE = "Hello " + NAME_PLACEHOLDER + ",  " +
            "  " +
            "welcome to the honorverse. If you want to survive, listen carefully.  " +
            "  " +
            "I suggest that you have a look at the planet you conquered from the pirates.  " +
            "You can replay the battle at the journals section to see the glorious victory of your admirals and crews.  " +
            "  " +
            "In order to improve the conditions for your colonists you should have a look if you can build some houses or hospitals.  " +
            "But keep in mind, you can only house as many persons as you can support.  " +
            "  " +
            "If you noticed, the universe is a hostile place so it could be a good idea to build a shipyard and build ships to control your space.  " +
            "  " +
            "  " +
            "Sincerely and with the best wishes,  " +
            DEFEATED_OPPONENT + "  ";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ResearchService researchService;

    @Nonnull
    private final ColonizationService colonizationService;

    @Nonnull
    private final PlanetService planetService;

    @Nonnull
    private final ChatService chatService;

    @Nonnull
    private final MasterOfTheUniverseService masterOfTheUniverseService;

    @Nonnull
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Nonnull
    private final AuthenticationManager authenticationManager;

    @Nonnull
    private final JwtTokenUtil jwtTokenUtil;

    @Nonnull
    private final OperationalService operationalService;

    @Nonnull
    private final MailService mailService;

    @Nonnull
    private final NonPlayerCharacterService nonPlayerCharacterService;

    @Nonnull
    private final HeatMapService heatMapService;

    @Nonnull
    private final TickTimeService tickTimeService;

    @Autowired
    public AuthApi(@Nonnull final AuthenticationManager authenticationManager,
                   @Nonnull final JwtTokenUtil jwtTokenUtil,
                   @Nonnull final UserService userService,
                   @Nonnull final ResearchService researchService,
                   @Nonnull final ColonizationService colonizationService,
                   @Nonnull final PlanetService planetService,
                   @Nonnull final ChatService chatService,
                   @Nonnull final MasterOfTheUniverseService masterOfTheUniverseService,
                   @Nonnull final OperationalService operationalService,
                   @Nonnull final MailService mailService,
                   @Nonnull final NonPlayerCharacterService nonPlayerCharacterService,
                   @Nonnull final HeatMapService heatMapService,
                   @Nonnull final TickTimeService tickTimeService) {
        this.authenticationManager = Preconditions.checkNotNull(authenticationManager, "authenticationManager shouldn't be null!");
        this.jwtTokenUtil = Preconditions.checkNotNull(jwtTokenUtil, "jwtTokenUtil shouldn't be null!");
        this.userService = Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        this.researchService = Preconditions.checkNotNull(researchService, "researchService shouldn't be null!");
        this.colonizationService = Preconditions.checkNotNull(colonizationService, "colonizationService shouldn't be null!");
        this.planetService = Preconditions.checkNotNull(planetService, "planetService shouldn't be null!");
        this.chatService = Preconditions.checkNotNull(chatService, "messageThreadService must not be empty");
        this.masterOfTheUniverseService = Preconditions.checkNotNull(masterOfTheUniverseService, "masterOfTheUniverseService must not be empty");
        this.operationalService = Preconditions.checkNotNull(operationalService, "operationalService must not be empty");
        this.mailService = Preconditions.checkNotNull(mailService, "mailService must not be empty");
        this.nonPlayerCharacterService = Preconditions.checkNotNull(nonPlayerCharacterService, "nonPlayerCharacterService must not be empty");
        this.heatMapService = Preconditions.checkNotNull(heatMapService, "heatMapService must not be empty");
        this.tickTimeService = Preconditions.checkNotNull(tickTimeService, "tickTimeService must not be empty");
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
            if (user.getUserSetting().isLoginForbidden()) {
                throw new NotifyWebUserException("Your login is prohibited.");
            }
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
            LOGGER.info(">>>Login: {} at {}", request.getUsername(), getNow());
            return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, accessToken).body(new JWT(user, accessToken, refreshToken));
        } catch (BadCredentialsException ex) {
            LOGGER.info(">>>FORBIDDEN: {} at {}", request.getUsername(), getNow());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private static String getNow() {
        return SDF.format(Calendar.getInstance().getTime());
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

    @PostMapping(value = "/requestPasswordChange")
    @Operation(summary = "Triggers the password change mail.", operationId = "requestPasswordChange",
            description = "Triggers the password change mail.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChangePassword.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public void requestPasswordChange(@RequestBody @Nonnull final ChangePassword changePassword) {
        PreconditionWebHelper.checkNotNull(changePassword, "changePassword shouldn't be null!");

        final User user = userService.findByUsernameOrEMail(changePassword.getUsername(), changePassword.geteMail());
        if (user != null) {
            mailService.sendMailChangePasswordMessage(user);
        }
    }

    @PostMapping("processPasswordChange/{code}/{newPassword}")
    @Operation(summary = "Changes the password.", operationId = "processPasswordChange")
    public void processPasswordChange(@PathVariable("code") @Nullable final String code,
                                      @PathVariable("newPassword") @Nullable final String newPassword,
                                      @Nonnull final HttpServletResponse httpServletResponse) {
        if (StringUtils.isBlank(code) || StringUtils.isBlank(newPassword)) {
            return;
        }

        final MailService.VerificationParameter params = MailService.getParametersFromVerificationCode(code);
        final User user = userService.find(params.getId());
        if (user != null) {
            if (params.verifyUser(user)) {
                userService.changePassword(user, newPassword);

                httpServletResponse.setHeader("Location", "https://www.battleforhonor.de/login");
                httpServletResponse.setStatus(302);
            }
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
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Player.class))),
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

        mailService.sendMailVerificationMessage(Objects.requireNonNull(saved));

        final List<Research> researchesWithoutPrecondition = researchService.getResearchesWithoutPrecondition();
        researchService.addResearchForNewAccounts(saved, researchesWithoutPrecondition);

        Planet planet = colonizationService.findPlanetForNewUser();
        planet = planetService.save(planet);
        final Colonization colonization = new Colonization(saved, planet, ColonizationCostCalculator.getCrewRequirementForColonization(), 0);
        planet = colonizationService.colonizePlanet(colonization);
        operationalService.operateInoperationals(tickTimeService.getToday(), planet);

        heatMapService.createHeatForMainPlanet(planet);

        final NonPlayerCharacter sender = nonPlayerCharacterService.findByUsername(DEFEATED_OPPONENT);
        final String replace = WELCOME_MESSAGE.replace(NAME_PLACEHOLDER, saved.getUsername());
        chatService.createChatMessage(sender, saved, replace);

        masterOfTheUniverseService.createOpponentAndFightAsync(saved);
        return ResponseEntity.ok(new Player(saved));
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

    @GetMapping("verify/{code}")
    @Operation(summary = "Checks if a eMail already exists.", operationId = "verifyEmail")
    public void verifyEmail(@PathVariable("code") @Nullable final String code,
                            @Nonnull final HttpServletResponse httpServletResponse) {
        if (StringUtils.isBlank(code)) {
            return;
        }

        final MailService.VerificationParameter params = MailService.getParametersFromVerificationCode(code);
        final User user = userService.find(params.getId());
        if (user != null) {
            if (params.verifyUser(user)) {
                userService.verifyEmail(user);

                httpServletResponse.setHeader("Location", "https://www.battleforhonor.de/login");
                httpServletResponse.setStatus(302);
            }
        }
    }
}
