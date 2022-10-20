package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = ".")
public class FileUpload {

    @JsonProperty
    @Schema(required = true, description = "The filename.")
    private final String fileName;


    @JsonProperty
    @Schema(required = true, description = "The filename.")
    private final String content;

    public FileUpload(final String fileName, final String content) {
        this.fileName = fileName;
        this.content = content;
    }
}
