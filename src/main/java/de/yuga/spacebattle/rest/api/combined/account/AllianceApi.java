package de.yuga.spacebattle.rest.api.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.account.forum.Forum;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.services.account.ForumService;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.config.role.AllowedRoles;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.combined.account.Alliance;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "AllianceApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + AllianceApi.ENDPOINT + "/")
public class AllianceApi extends BaseApi {

    @Nonnull
    public static final String ENDPOINT = "alliances";
    private static final String APPLY_FOR_MEMBERSHIP = "membership";

    @Nonnull
    private final AllianceService allianceService;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final ForumService forumService;

    @Autowired
    public AllianceApi(@Nonnull final AllianceService allianceService,
                       @Nonnull final UserService userService,
                       @Nonnull final ForumService forumService) {
        Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(forumService, "forumService shouldn't be null!");

        this.allianceService = allianceService;
        this.userService = userService;
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
        final List<de.yuga.spacebattle.backend.entities.combined.account.Alliance> all = allianceService.findAllWithMembers();
        return ResponseEntity.ok(all.stream().map(a -> new Alliance(a, a.getMembers().size())));
    }

    @GetMapping("/{idAlliance}")
    @Operation(summary = "Gets an alliance.", operationId = "getAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Alliance.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAlliance(@PathVariable("idAlliance") final int idAlliance) {
        de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = allianceService.findWithMembers(idAlliance);
        if (alliance == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(new Alliance(alliance, alliance.getMembers().size()));
    }

    @GetMapping("/members/{idAlliance}")
    @Operation(summary = "Gets an alliances.", operationId = "getMembers",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Player.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getMembers(@PathVariable("idAlliance") final int idAlliance) {
        de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = allianceService.findWithMembers(idAlliance);
        if (alliance == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(alliance.getMembers().stream().map(Player::new).collect(Collectors.toList()));
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
    public ResponseEntity<?> getAllianceForUser() {
        final int idUser = getIdUser();
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
    public ResponseEntity<?> createAlliance(@PathVariable("name") final String name,
                                            @PathVariable("code") final String code) {

        final int idUser = getIdUser();
        final User user = userService.find(idUser);
        PreconditionWebHelper.checkNotNull(user, "user shouldn't be null!");

        de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = new de.yuga.spacebattle.backend.entities.combined.account.Alliance(name, code, user);
        alliance = allianceService.save(alliance);
        user.setAlliance(alliance);
        userService.save(user);
        final Forum forum = new Forum(alliance, alliance.getName() + "'s forum", "The Forum for the " + alliance.getName());
        forumService.save(forum);
        return ResponseEntity.ok(new Alliance(alliance));
    }

    @PostMapping(APPLY_FOR_MEMBERSHIP + "/{idUserToAdd}")
    @AllowedRoles(roles = EGameUserRole.ALLIANCE_ADMIN)
    @Operation(summary = "Grants the application of a user to an alliances.", operationId = "grantApplication",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> grantApplication(@PathVariable("idUserToAdd") final int idUserToAdd) {
        final int idUser = getIdUser();
        allianceService.grantApplication(idUser, idUserToAdd);
        return ResponseEntity.ok(true);
    }

    @AllowedRoles(roles = EGameUserRole.ALLIANCE_ADMIN)
    @DeleteMapping(APPLY_FOR_MEMBERSHIP + "/{idUserToRemove}")
    @Operation(summary = "Denies the application of user to an alliances.", operationId = "denyApplication",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> denyApplication(@PathVariable("idUserToRemove") final int idUserToRemove) {
        final int idUser = getIdUser();
        allianceService.denyApplication(idUser, idUserToRemove);
        return ResponseEntity.ok(true);
    }

    @AllowedRoles(roles = EGameUserRole.ALLIANCE_ADMIN)
    @GetMapping(APPLY_FOR_MEMBERSHIP + "/application")
    @Operation(summary = "Fetches the applications of users to the alliance of the ally admin.", operationId = "getApplicationsForMembership",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = Player.class)))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getApplicationsForMembership() {

        final int idUser = getIdUser();
        final Set<User> applications = allianceService.findOpenApplicationsForAlliance(idUser);
        return ResponseEntity.ok(applications.stream().map(Player::new).collect(Collectors.toList()));
    }

    @GetMapping(APPLY_FOR_MEMBERSHIP + "/isApplicant")
    @Operation(summary = "Returns where the user has an application to an alliance open.", operationId = "getOpenApplications",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Alliance.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getOpenApplications() {
        return ResponseEntity.ok(allianceService.getOpenApplications(getIdUser()).stream().map(Alliance::new).collect(Collectors.toList()));
    }

    @PutMapping(APPLY_FOR_MEMBERSHIP + "/{idAlliance}")
    @Operation(summary = "Starts the application of a user to an alliances.", operationId = "applyForMembership",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> applyForMembership(@PathVariable("idAlliance") final int idAlliance) {
        final int idUser = getIdUser();
        allianceService.applyForMembership(idUser, idAlliance);
        return ResponseEntity.ok(true);
    }

    @PutMapping("leave")
    @Operation(summary = "Starts the application of a user to an alliances.", operationId = "leaveAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> leaveAlliance() {
        final int idUser = getIdUser();
        allianceService.leave(idUser);
        return ResponseEntity.ok(true);
    }

    @PutMapping(APPLY_FOR_MEMBERSHIP + "/withdraw/{idAlliance}")
    @Operation(summary = "Starts the application of a user to an alliances.", operationId = "withdrawApplication",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> withdrawApplication(@PathVariable("idAlliance") final int idAlliance) {
        final int idUser = getIdUser();
        allianceService.withdrawApplication(idUser, idAlliance);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping
    @AllowedRoles(roles = EGameUserRole.ALLIANCE_ADMIN)
    @Operation(summary = "Adds a user to an alliances.", operationId = "deleteAlliance",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> deleteAlliance() {
        final User user = userService.find(getIdUser());
        assert user != null : "Yeah if you are an alliance admin, you are present.";
        final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance = user.getAlliance();
        assert alliance != null : "An alliance admin should have an alliance.";
        final Forum allianceForum = forumService.getAllianceForumForUser(alliance);
        forumService.delete(allianceForum);
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
