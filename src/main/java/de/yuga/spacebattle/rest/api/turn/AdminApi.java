package de.yuga.spacebattle.rest.api.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.services.turn.TickService;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.turn.Tick;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.ADMIN_BASE_ENDPOINT;

@Tag(name = "AdminApi")
@RolesAllowed("ADMIN")
@RestController
@RequestMapping(value = "/" + ADMIN_BASE_ENDPOINT + "/" + AdminApi.ENDPOINT + "/")
public class AdminApi {

    @Nonnull
    public static final String ENDPOINT = "admin";

    private final static Logger LOGGER = LoggerFactory.getLogger(AdminApi.class);

    @Nonnull
    private final TickService tickController;

    @Autowired
    public AdminApi(@Nonnull final TickService tickController) {
        Preconditions.checkNotNull(tickController, "tickC shouldn't be null!");

        this.tickController = tickController;
    }

    @GetMapping(value = "/doTick")
    @Operation(summary = "Get the current tick.", operationId = "doTick",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Tick.class))),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    @ResponseBody
    public ResponseEntity<?> doTick() {
        LOGGER.info("Tick initialized");
        final de.yuga.spacebattle.backend.entities.turn.Tick now = tickController.doTick();
        return ResponseEntity.ok(new Tick(now));
    }
}
