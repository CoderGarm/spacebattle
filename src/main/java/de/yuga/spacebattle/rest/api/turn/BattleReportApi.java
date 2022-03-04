package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.battle.BattleReportService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.battle.collections.BattleReportList;
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

@Api(tags = "BattleReportApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + BattleReportApi.ENDPOINT + "/")
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
    @ApiOperation(value = "Get all fighting reports for the user.", nickname = "getReportsWithUserWithPaging")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BattleReportList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getReportsWithUserWithPaging(@PathVariable("idUser") final int idUser,
                                                          @PathVariable("page") final int page,
                                                          @PathVariable("size") final int size) {
        final List<de.yuga.spacebattle.backend.entities.turn.battle.BattleReport> battleReportsWithUser = battleReportService.findReportsWithUserWithPaging(idUser, page, size);
        return ResponseEntity.ok(new BattleReportList(battleReportsWithUser));
    }
}
