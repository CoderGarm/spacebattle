package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Schema(description = ".")
public class JWT {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The access token to authenticate every request against the backend.")
    private String accessToken;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The access token to authenticate every request against the backend.")
    private String refreshToken;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's name.")
    private String username;

    @JsonProperty
    @Schema(required = true, description = "The user's ID.")
    private int idUser;

    @Nullable
    @JsonProperty
    @Schema(description = "The id of the user's alliance.")
    private Integer idAlliance;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's role.")
    private EWebUserRole role;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's in-game roles.")
    private Set<EGameUserRole> gameUserRoles = new HashSet<>();

    public JWT() {
    }

    public JWT(@Nonnull final User user, @Nonnull final String accessToken, @Nonnull final String refreshToken) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(accessToken, "accessToken shouldn't be null!");
        Preconditions.checkNotNull(refreshToken, "refreshToken shouldn't be null!");

        this.username = user.getUsername();
        this.idUser = user.getId();
        if (user.getAlliance() != null) {
            this.idAlliance = user.getAlliance().getId();
        }
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = user.getUserRole();
        this.gameUserRoles = user.getGameUserRoles();
    }
}
