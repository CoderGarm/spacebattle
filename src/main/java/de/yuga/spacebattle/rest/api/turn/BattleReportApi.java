package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.BattleReportCache;
import de.yuga.spacebattle.backend.services.turn.TickTimeService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import de.yuga.spacebattle.rest.dto.turn.battle.ChangeSharedBattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.SharedBattleReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
public class BattleReportApi extends BaseApi {

    public static final String ENDPOINT = "report";
    public static final String FIGHTING_ENDPOINT = "battle";
    public static final String HAS_NEW_REPORTS_ENDPOINT = "hasNew";
    public static final String FIGHTING_BY_ID_ENDPOINT = FIGHTING_ENDPOINT + "/byId";
    public static final String FIGHTING_AMOUNT_ENDPOINT = FIGHTING_ENDPOINT + "/amount";
    public static final String SHARE_REPORTS_ENDPOINT = "share";

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final TickTimeService tickService;

    @Nonnull
    private final BattleReportCache battleReportCache;

    @Autowired
    public BattleReportApi(@Nonnull final BattleReportService battleReportService,
                           @Nonnull final TickTimeService tickService,
                           @Nonnull final BattleReportCache battleReportCache) {
        this.battleReportService = Preconditions.checkNotNull(battleReportService, "battleReportService must not be empty");
        this.tickService = Preconditions.checkNotNull(tickService, "tickService must not be empty");
        this.battleReportCache = Preconditions.checkNotNull(battleReportCache, "battleReportCache must not be empty");
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
    public ResponseEntity<?> getReportsAmountWithUser() {

        final int idUser = getIdUser();
        return ResponseEntity.ok(battleReportService.countAllWithUser(idUser));
    }

    @GetMapping(value = FIGHTING_ENDPOINT)
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = BattleReportStatistics.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getReportsForUser() {

        final Collection<BattleReportStatistics> reports = battleReportService.findAllReportsBasicInformationForUser(getIdUser());
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
    public ResponseEntity<?> getReportsById(@PathVariable("idBattleReport") final int idBattleReport) {

        final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport = battleReportService.findByIdWithAllData(idBattleReport);
        if (battleReport == null) {
            throw new NotifyWebUserException("Nothing there, buddy.");
        }
        return ResponseEntity.ok(new BattleReport(battleReport, getPreferredLanguage()));
    }

    @GetMapping(value = HAS_NEW_REPORTS_ENDPOINT)
    @Operation(summary = "Returns if there are unknown battle reports.", operationId = "hasNewReportsForUser",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> hasNewReportsForUser() {

        final int idUser = getIdUser();
        final Tick today = tickService.getToday();
        final Integer lastQueried = battleReportCache.getLastQueryBattleReports(idUser);
        battleReportCache.setLastQueryBattleReports(today, idUser);
        if (lastQueried != null && lastQueried.equals(today.getNo())) {
            return ResponseEntity.ok(false);
        }
        final boolean hasNewReportsSince = battleReportService.hasNewReportsSince(idUser, lastQueried != null ? lastQueried : today.getNo());
        return ResponseEntity.ok(hasNewReportsSince);
    }

    @GetMapping(value = SHARE_REPORTS_ENDPOINT + "/{idBattleReport}")
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportSharings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = SharedBattleReport.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getReportSharings(@PathVariable("idBattleReport") final int idBattleReport) {

        final SharedBattleReport sharedReport = battleReportService.findSharedReport(idBattleReport);
        if (sharedReport == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(sharedReport);
    }


    @PutMapping(value = SHARE_REPORTS_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all fighting reports for the user.", operationId = "changeReportSharings",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> changeReportSharings(@RequestBody @Nonnull final ChangeSharedBattleReport change) {
        Preconditions.checkNotNull(change, "change must not be empty");

        battleReportService.changeReportSharings(change);
        return ResponseEntity.ok(true);
    }
}
