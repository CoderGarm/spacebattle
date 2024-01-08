package de.yuga.spacebattle.rest.api.events;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import de.yuga.spacebattle.backend.services.events.RankingService;
import de.yuga.spacebattle.rest.api.BaseApi;
import de.yuga.spacebattle.rest.dto.error.FrontendError;
import de.yuga.spacebattle.rest.dto.events.EventRanking;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.rest.api.EndpointDefinition.PRIVATE_BASE_ENDPOINT;

@RestController
@Tag(name = "GameEventApi")
@RequestMapping(value = "/" + PRIVATE_BASE_ENDPOINT + "/" + GameEventApi.ENDPOINT + "/")
public class GameEventApi extends BaseApi {

    public static final String ENDPOINT = "game-event";

    @Nonnull
    private final RankingService rankingService;

    @Autowired
    public GameEventApi(@Nonnull final RankingService rankingService) {
        this.rankingService = Preconditions.checkNotNull(rankingService, "rankingService must not be empty");
    }

    @GetMapping
    @Operation(summary = "Get all jobs which finished today.", operationId = "getEventRanking",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(
                                    schema = @Schema(implementation = EventRanking.class))
                            )),
                    @ApiResponse(responseCode = "400", description = "an error occurred",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = FrontendError.class)))
            }
    )
    public ResponseEntity<?> getEventRanking() {
        final Set<de.yuga.spacebattle.backend.entities.events.EventRanking> rankings = rankingService.findAll(EGameEvent.WAR_HARVEST_23);
        return ResponseEntity.ok(rankings.stream().map(EventRanking::new).collect(Collectors.toList()));
    }

}
