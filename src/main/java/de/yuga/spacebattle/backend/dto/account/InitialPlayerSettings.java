package de.yuga.spacebattle.backend.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.entities.account.UserSetting;
import de.yuga.spacebattle.rest.dto.account.RolePlayData;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class InitialPlayerSettings {

    @JsonProperty
    @Schema(required = true, description = "If the user want to receive infos about a new released version via mail.")
    private boolean receiveChangelogInfos;

    @JsonProperty
    @Schema(required = true, description = "If the user want to receive infos about the current's tick advice.")
    private boolean receiveTickAdvice;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "Some information about role playing.")
    private RolePlayData rolePlayData;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The profile pic name.")
    private String profilePic = UserSetting.DEFAULT_PROFILE_PIC;

    public InitialPlayerSettings() {
    }


    public boolean isReceiveChangelogInfos() {
        return receiveChangelogInfos;
    }

    public boolean isReceiveTickAdvice() {
        return receiveTickAdvice;
    }

    @Nonnull
    public RolePlayData getRolePlayData() {
        return rolePlayData;
    }

    @Nonnull
    public String getProfilePic() {
        return profilePic;
    }
}
