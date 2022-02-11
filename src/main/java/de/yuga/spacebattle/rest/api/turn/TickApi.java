package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import de.yuga.spacebattle.rest.dto.turn.TickList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.List;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;
import static de.yuga.spacebattle.rest.api.turn.AdminApi.ENDPOINT;

@Api(tags = "TickApi")
@RolesAllowed("ROLE_USER")
@RestController
@RequestMapping("/" + PRIVATE_BASE_ENDPOINT + "/" + ENDPOINT + "/")
public class TickApi {

    @Nonnull
    public static final String ENDPOINT = "tick";

    private final static Logger LOGGER = LoggerFactory.getLogger(TickApi.class);

    @Nonnull
    private final TickService tickService;

    @Autowired
    public TickApi(@Nonnull final TickService tickService) {
        Preconditions.checkNotNull(tickService, "tickService shouldn't be null!");

        this.tickService = tickService;
    }

    @GetMapping(value = "current")
    @ApiOperation(value = "Get the current tick.", nickname = "getCurrentTick")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getCurrentTick() {
        final de.yuga.spacebattle.backend.entities.turn.Tick latest = tickService.getLatest();
        PreconditionWebHelper.checkNotNull(latest, "There should be at least one tick - please call the admin.");
        return ResponseEntity.ok(new Tick(latest));
    }

    @GetMapping(value = "all")
    @ApiOperation(value = "Get all ticks.", nickname = "getAllTicks")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TickList.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getAllTicks() {
        final List<de.yuga.spacebattle.backend.entities.turn.Tick> all = tickService.findAll();
        return ResponseEntity.ok(new TickList(all));
    }

    @GetMapping("/{idTick}")
    @ApiOperation(value = "Get the current tick.", nickname = "getTick")
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> getTick(@PathVariable("idTick") final int idTick) {
        de.yuga.spacebattle.backend.entities.turn.Tick tick = tickService.find(idTick);
        if (tick == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new Tick(tick));
    }
}
