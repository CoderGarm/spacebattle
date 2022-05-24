package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "BattleReportApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + BattleReportApi.ENDPOINT + "/")
public class BattleReportApi {

    @Nonnull
    public static final String ENDPOINT = "report";
    public static final String FIGHTING_ENDPOINT = "battle";

    private final static Logger LOGGER = LoggerFactory.getLogger(BattleReportApi.class);

    @Nonnull
    private final BattleReportService battleReportService;

    @Autowired
    public BattleReportApi(@Nonnull final BattleReportService battleReportService) {
        Preconditions.checkNotNull(battleReportService, "fightingReportService shouldn't be null!");

        this.battleReportService = battleReportService;
    }

    @GetMapping(value = FIGHTING_ENDPOINT + "/{idUser}/{page}/{size}")
    @Operation(summary = "Get all fighting reports for the user.", operationId = "getReportsWithUserWithPaging",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = BattleReport.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getReportsWithUserWithPaging(@PathVariable("idUser") final int idUser,
                                                          @PathVariable("page") final int page,
                                                          @PathVariable("size") final int size) {
        return ResponseEntity.ok(battleReportService.findReportsWithUserWithPaging(idUser, page, size).stream().map(BattleReport::new).collect(Collectors.toList()));
    }
}
