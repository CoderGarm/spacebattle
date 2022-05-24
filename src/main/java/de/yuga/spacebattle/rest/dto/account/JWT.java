package de.yuga.spacebattle.rest.dto.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class JWT {

    @Nonnull
    @Schema(required = true, description = "The access token to authenticate every request against the backend.")
    private String accessToken;

    @Nonnull
    @Schema(required = true, description = "The access token to authenticate every request against the backend.")
    private String refreshToken;

    @Nonnull
    @Schema(required = true, description = "The user's name.")
    private String username;

    @Schema(required = true, description = "The user's ID.")
    private int idUser;

    @Nonnull
    @Schema(required = true, description = "The user's role.")
    private EWebUserRole role;

    public JWT() {
    }

    public JWT(@Nonnull final User user, @Nonnull final String accessToken, @Nonnull final String refreshToken) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(accessToken, "accessToken shouldn't be null!");
        Preconditions.checkNotNull(refreshToken, "refreshToken shouldn't be null!");

        this.username = user.getUsername();
        this.idUser = user.getId();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = EWebUserRole.USER;
    }

    @Nonnull
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(@Nonnull String accessToken) {
        this.accessToken = accessToken;
    }

    @Nonnull
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(@Nonnull String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nonnull String username) {
        this.username = username;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    @Nonnull
    public EWebUserRole getRole() {
        return role;
    }

    public void setRole(@Nonnull EWebUserRole role) {
        this.role = role;
    }
}
