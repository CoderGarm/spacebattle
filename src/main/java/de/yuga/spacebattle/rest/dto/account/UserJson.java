package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

/**
 * The simplest representation of a user.
 */
@Schema(description = ".")
public class UserJson {

    @Nullable
    @Schema(required = true, description = "The user's database id.")
    private Integer idUser;

    @Nullable
    @JsonProperty
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @Schema(required = true, description = "The user's name")
    private String username;

    @Nullable
    @JsonProperty
    @Schema(description = "The user's alliance.")
    private Integer idAlliance;

    @Nullable
    @JsonProperty
    @Schema(description = "The user's role.")
    private String role;

    public UserJson() {
    }

    public UserJson(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.idUser = user.getId();
        this.username = user.getUsername();
        this.idAlliance = user.getAlliance() != null ? user.getAlliance().getId() : null;
        this.role = user.getUserRole().getName();
    }

    @Nullable
    public Integer getIdUser() {
        return idUser;
    }
}
