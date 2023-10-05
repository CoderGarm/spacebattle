package de.yuga.spacebattle.backend.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.UserSetting;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class UserSettings {

    @JsonProperty
    @Schema(required = true, description = "If the user want to receive infos about a new released version via mail.")
    private boolean receiveChangelogInfos;

    @JsonProperty
    @Schema(required = true, description = "If the user must not log in.")
    private boolean loginForbidden;

    @JsonProperty
    @Schema(required = true, description = "If the user has verified the mail address.")
    private boolean eMailVerified;

    @JsonProperty
    @Schema(description = "The mail address.")
    private String eMail;

    @JsonProperty
    @Schema(required = true, description = "If the user does not want receive any mail.")
    private boolean noEMailWanted;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The profile pic name.")
    private String profilePic = UserSetting.DEFAULT_PROFILE_PIC;

    public UserSettings() {
    }

    public UserSettings(@Nonnull final UserSetting userSetting) {
        Preconditions.checkNotNull(userSetting, "userSetting must not be empty");

        this.receiveChangelogInfos = userSetting.isReceiveChangelogInfos();
        this.loginForbidden = userSetting.isLoginForbidden();
        this.eMailVerified = userSetting.isEMailVerified();
        this.eMail = userSetting.getEmail();
        this.noEMailWanted = userSetting.isNoEMailWanted();
        this.profilePic = userSetting.getProfilePic();
    }

    public boolean isReceiveChangelogInfos() {
        return receiveChangelogInfos;
    }

    @Nonnull
    public String getProfilePic() {
        return profilePic;
    }
}
