package de.yuga.spacebattle.rest.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ApplicationInfo {

    @JsonProperty
    @Schema(required = true, description = "The application version.")
    private String applicationVersion;

    public ApplicationInfo(@Nonnull final String applicationVersion) {
        Preconditions.checkNotNull(applicationVersion, "applicationVersion must not be empty");

        this.applicationVersion = applicationVersion;
    }

}
