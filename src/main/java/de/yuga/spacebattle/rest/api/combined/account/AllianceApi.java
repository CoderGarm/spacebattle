package de.yuga.spacebattle.rest.api.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import de.yuga.spacebattle.rest.dto.combined.account.Alliance;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "AllianceApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + AllianceApi.ENDPOINT + "/")
public class AllianceApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(AllianceApi.class);

    @Nonnull
    public static final String ENDPOINT = "alliances";

    @Nonnull
    private final AllianceService allianceService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ForumService forumService;

    @Nonnull
    private final JwtTokenUtil tokenUtil;

    @Autowired
    public AllianceApi(@Nonnull final AllianceService allianceService,
                       @Nonnull final UserService userService,
                       @Nonnull final ForumService forumService,
                       @Nonnull final JwtTokenUtil tokenUtil) {
        Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(tokenUtil, "tokenUtil shouldn't be null!");
        Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");

        this.allianceService = allianceService;
        this.userService = userService;
        this.tokenUtil = tokenUtil;
        this.forumService = forumService;
    }


    @GetMapping
    @Operation(summary = "Get all alliances.", operationId = "getAlliances",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Alliance.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAlliances() {
        final List<de.yuga.spacebattle.backend.entities.combined.account.Alliance> all = allianceService.findAll();
        return ResponseEntity.ok(all.stream().map(Alliance::new));
    }

    @GetMapping("/{idAlliance}")
    @Operation(summary = "Gets an alliances.", operationId = "getAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Alliance.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAlliance(@PathVariable("idAlliance") final int idAlliance) {
        de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = allianceService.find(idAlliance);
        if (alliance == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(new Alliance(alliance));
    }

    @GetMapping("forUser")
    @Operation(summary = "Gets an alliances.", operationId = "getAllianceForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Alliance.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllianceForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token) {
        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
        final User user = userService.find(idUser);
        PreconditionWebHelper.checkNotNull(user, "user shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = user.getAlliance();
        if (alliance == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(new Alliance(alliance));
    }

    @PostMapping("/{name}/{code}")
    @Operation(summary = "Create an alliances.", operationId = "createAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Alliance.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> createAlliance(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                            @PathVariable("name") final String name,
                                            @PathVariable("code") final String code) {

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
        final User user = userService.find(idUser);
        PreconditionWebHelper.checkNotNull(user, "user shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = new de.yuga.spacebattle.backend.entities.combined.account.Alliance(name, code, user);
        allianceService.save(alliance);
        userService.save(user);
        final Forum forum = new Forum(alliance, alliance.getName() + "'s forum", "The Forum for the " + alliance.getName());
        forumService.save(forum);
        return ResponseEntity.ok(new Alliance(alliance));
    }

    @PutMapping("/{idAlliance}/{idUserToAdd}")
    @Operation(summary = "Adds a user to an alliances.", operationId = "addUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> addUser(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                     @PathVariable("idAlliance") final int idAlliance,
                                     @PathVariable("idUserToAdd") final int idUserToAdd) {
        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = checkFounderActionAndGet(token, idAlliance);

        final User toAdd = userService.find(idUserToAdd);
        Preconditions.checkNotNull(toAdd, "toAdd shouldn't be null!");

        toAdd.setAlliance(alliance);
        userService.save(toAdd);

        return ResponseEntity.ok(true);
    }

    private de.yuga.spacebattle.backend.entities.combined.account.Alliance checkFounderActionAndGet(@Nonnull final String token, final int idAlliance) {
        Preconditions.checkNotNull(token, "token shouldn't be null!");

        final int idUser = tokenUtil.getIdUserFromAccessToken(token);
        final User user = userService.find(idUser);
        PreconditionWebHelper.checkNotNull(user, "user shouldn't be null!");

        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = allianceService.find(idAlliance);
        if (alliance == null || !alliance.getFounder().equals(user)) {
            LOGGER.warn("The idUser '" + idUser + "' tries to cheat!");
            throw new NotifyWebUserException("Nop, this is not permitted.");
        }
        return alliance;
    }

    @DeleteMapping("/{idAlliance}")
    @Operation(summary = "Adds a user to an alliances.", operationId = "deleteAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> deleteAlliance(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                            @PathVariable("idAlliance") final int idAlliance) {
        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = checkFounderActionAndGet(token, idAlliance);
        final Forum allianceForumForUser = forumService.getAllianceForumForUser(alliance);
        forumService.delete(allianceForumForUser);
        allianceService.delete(alliance);
        return ResponseEntity.ok(true);
    }


    @PostMapping("/checkAllianceName/{name}")
    @Operation(summary = "Checks if a username already exists.", operationId = "checkAllianceName",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> checkAllianceName(@PathVariable("name") @Nullable final String name) {
        if (StringUtils.isBlank(name) || allianceService.existsAllianceName(name)) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/checkEmail/{eMail}")
    @Operation(summary = "Checks if a eMail already exists.", operationId = "checkCode",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> checkCode(@PathVariable("eMail") @Nullable final String eMail) {
        if (StringUtils.isBlank(eMail) || allianceService.existsAllianceCode(eMail)) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }
}
