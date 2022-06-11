package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.config.security.JwtTokenUtil;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.Collection;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "BattleReportApi")
@RolesAllowed("USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + BattleReportApi.ENDPOINT + "/")
public class BattleReportApi {

    @Nonnull
    public static final String ENDPOINT = "report";
    public static final String FIGHTING_ENDPOINT = "battle";
    public static final String FIGHTING_BY_ID_ENDPOINT = "battle/byId";
    public static final String FIGHTING_AMOUNT_ENDPOINT = "battle/amount";

    private final static Logger LOGGER = LoggerFactory.getLogger(BattleReportApi.class);

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final JwtTokenUtil jwtTokenUtil;

    @Autowired
    public BattleReportApi(@Nonnull final BattleReportService battleReportService,
                           @Nonnull final JwtTokenUtil jwtTokenUtil) {
        Preconditions.checkNotNull(battleReportService, "fightingReportService shouldn't be null!");
        Preconditions.checkNotNull(jwtTokenUtil, "jwtTokenUtil shouldn't be null!");

        this.battleReportService = battleReportService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @GetMapping(value = FIGHTING_AMOUNT_ENDPOINT)
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportsAmountWithUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Integer.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getReportsAmountWithUser(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token) {
        PreconditionWebHelper.checkNotNull(token, "token shouldn't be null!");

        final int idUser = jwtTokenUtil.getIdUserFromAccessToken(token);
        return ResponseEntity.ok(battleReportService.countAllWithUser(idUser));
    }

    @GetMapping(value = FIGHTING_ENDPOINT + "/{page}/{size}")
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportsWithUserWithPaging",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = BattleReportStatistics.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getReportsWithUserWithPaging(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                                          @PathVariable("page") final int page,
                                                          @PathVariable("size") final int size) {
        PreconditionWebHelper.checkNotNull(token, "token shouldn't be null!");

        final int idUser = jwtTokenUtil.getIdUserFromAccessToken(token);
        final Collection<BattleReportStatistics> reports = battleReportService.findReportBasicInformationByPaging(idUser, page, size);
        return ResponseEntity.ok(reports);
    }

    @GetMapping(value = FIGHTING_BY_ID_ENDPOINT + "/{idBattleReport}")
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportsById",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BattleReport.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getReportsById(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) @Nonnull final String token,
                                            @PathVariable("idBattleReport") final int idBattleReport) {
        PreconditionWebHelper.checkNotNull(token, "token shouldn't be null!");

        final int idUser = jwtTokenUtil.getIdUserFromAccessToken(token);
        final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport = battleReportService.findByIdWithAllData(idUser, idBattleReport);
        if (battleReport == null) {
            throw new NotifyWebUserException("Nothing there, buddy.");
        }
        return ResponseEntity.ok(new BattleReport(battleReport));
    }
}
