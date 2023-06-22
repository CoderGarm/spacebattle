package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.services.caches.BattleReportCache;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
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

    @Nonnull
    private final BattleReportService battleReportService;

    @Nonnull
    private final TickService tickService;

    @Nonnull
    private final BattleReportCache battleReportCache;

    @Autowired
    public BattleReportApi(@Nonnull final BattleReportService battleReportService,
                           @Nonnull final TickService tickService,
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
    public ResponseEntity<?> getReportsWithUserWithPaging(@PathVariable("page") final int page,
                                                          @PathVariable("size") final int size) {

        final int idUser = getIdUser();
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
    public ResponseEntity<?> getReportsById(@PathVariable("idBattleReport") final int idBattleReport) {

        final int idUser = getIdUser();
        final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport battleReport = battleReportService.findByIdWithAllData(idUser, idBattleReport);
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
        final Tick lastQueried = battleReportCache.getLastQueryBattleReports(idUser);
        battleReportCache.setLastQueryBattleReports(today, idUser);
        if (lastQueried == null || lastQueried.equals(today)) {
            // has already asked today or never asked before
            return ResponseEntity.ok(false);
        }
        final boolean hasNewReportsSince = battleReportService.hasNewReportsSince(idUser, lastQueried);
        return ResponseEntity.ok(hasNewReportsSince);
    }
}
