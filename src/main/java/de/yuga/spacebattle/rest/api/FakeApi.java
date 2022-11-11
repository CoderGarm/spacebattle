package de.yuga.spacebattle.rest.api;

import de.yuga.spacebattle.rest.dto.misc.EnumValueDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "FakeApi")
public class FakeApi {

    @GetMapping(value = "enumValue")
    @Operation(summary = "Get all relevant enum values.", operationId = "getEnumValues",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EnumValueDto.class)))
            }
    )
    public void getEnumValues() {

    }
}
