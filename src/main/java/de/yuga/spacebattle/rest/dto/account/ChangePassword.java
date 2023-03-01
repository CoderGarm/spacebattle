package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ChangePassword {

    @Nonnull
    @JsonProperty
    @Schema(description = "The user's name")
    private String username;

    @Nonnull
    @JsonProperty
    @Schema(description = "The user's eMail.")
    private String eMail;

    public ChangePassword() {
    }

    @Nonnull
    @JsonIgnore
    public String getUsername() {
        return username;
    }

    @Nonnull
    @JsonIgnore
    public String geteMail() {
        return eMail;
    }
}
