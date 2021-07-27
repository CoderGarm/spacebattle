package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Api(tags = "ReportApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ReportApi.ENDPOINT + "/")
public class ReportApi {

    @Nonnull
    public static final String ENDPOINT = "report";
    public static final String FIGHTING_ENDPOINT = "battle";

    private final static Logger LOGGER = LoggerFactory.getLogger(ReportApi.class);

    @Nonnull
    private final TickService tickController;

    @Nonnull
    private final BattleReportService battleReportService;

    @Autowired
    public ReportApi(@Nonnull final TickService tickController,
                     @Nonnull BattleReportService battleReportService) {
        Preconditions.checkNotNull(tickController, "tickC shouldn't be null!");
        Preconditions.checkNotNull(battleReportService, "fightingReportService shouldn't be null!");

        this.tickController = tickController;
        this.battleReportService = battleReportService;
    }

    @GetMapping(value = FIGHTING_ENDPOINT + "/{idUser}")
    @ApiOperation(value = "Get all fighting reports for the user.", nickname = "getAllReportsWithUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BattleReportList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getAllReportsWithUser(@PathVariable("idUser") final int idUser) {
        final List<de.yuga.spacebattle.backend.entities.turn.battle.BattleReport> battleReportsWithUser = battleReportService.findAllWithUser(idUser);
        return ResponseEntity.ok(new BattleReportList(battleReportsWithUser));
    }

    @GetMapping(value = FIGHTING_ENDPOINT + "/latest/{idUser}")
    @ApiOperation(value = "Get all fighting reports for the user.", nickname = "getLatestReportsWithUser")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BattleReport.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getLatestReportsWithUser(@PathVariable("idUser") final int idUser) {
        final de.yuga.spacebattle.backend.entities.turn.battle.BattleReport latestBattleReportsWithUser = battleReportService.findLatestWithUser(idUser);
        return ResponseEntity.ok(new BattleReport(latestBattleReportsWithUser));
    }
}
