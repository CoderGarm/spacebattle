package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.api.PreconditionWebHelper;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@Tag(name = "TickApi")
@RolesAllowed({"USER"})
@RestController
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + TickApi.ENDPOINT + "/")
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
    @Operation(summary = "Get the current tick.", operationId = "getCurrentTick",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getCurrentTick() {
        final de.yuga.spacebattle.backend.entities.turn.Tick latest = tickService.getLatest();
        PreconditionWebHelper.checkNotNull(latest, "There should be at least one tick - please call the admin.");
        return ResponseEntity.ok(new Tick(latest));
    }

    @GetMapping(value = "all")
    @Operation(summary = "Get all ticks.", operationId = "getAllTicks",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = Tick.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getAllTicks() {
        return ResponseEntity.ok(tickService.findAll().stream().map(Tick::new).collect(Collectors.toList()));
    }

    @GetMapping("/{idTick}")
    @Operation(summary = "Get the current tick.", operationId = "getTick",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getTick(@PathVariable("idTick") final int idTick) {
        de.yuga.spacebattle.backend.entities.turn.Tick tick = tickService.find(idTick);
        if (tick == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new Tick(tick));
    }
}
