package de.yuga.spacebattle.rest.dto.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class JWT {

    @Nonnull
    @ApiModelProperty(required = true, value = "The access token to authenticate every request against the backend.")
    private String accessToken;

    @Nonnull
    @ApiModelProperty(required = true, value = "The access token to authenticate every request against the backend.")
    private String refreshToken;

    @Nonnull
    @ApiModelProperty(required = true, value = "The user's name.")
    private String username;

    @ApiModelProperty(required = true, value = "The user's ID.")
    private int idUser;

    @Nonnull
    @ApiModelProperty(required = true, value = "The user's role.")
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
