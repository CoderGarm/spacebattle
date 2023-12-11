package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;

/**
 * The simplest representation of a user.
 */
@Schema(description = ".")
public class Player {

    @JsonProperty
    @Schema(required = true, description = "The user's database id.")
    private int idUser;

    @JsonProperty
    @Schema(required = true, description = "If the player is NPC or human.")
    private boolean isNpc;

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
    @Schema(description = "The user's alliance.")
    private String allianceTag;

    @Nullable
    @JsonProperty
    @Schema(description = "The user's role.")
    private String role;

    @Nullable
    @JsonProperty
    @Schema(description = "The user's profile pic.")
    private String profilePic;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's rpg settings.")
    private RolePlayData rolePlayData = new RolePlayData();

    public Player() {
    }

    public Player(@Nonnull final Owner user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.idUser = user.getId();
        this.username = user.getUsername();
        if (user instanceof User) {
            this.idAlliance = ((User) user).getAlliance() != null ? ((User) user).getAlliance().getId() : null;
            this.allianceTag = ((User) user).getAlliance() != null ? ((User) user).getAlliance().getCode() : null;
            this.role = ((User) user).getUserRole().getName();
            this.profilePic = ((User) user).getUserSetting().getProfilePic();
            this.rolePlayData.setFirstname(((User) user).getRolePlaySetting().getFirstname());
            this.rolePlayData.setSurname(((User) user).getRolePlaySetting().getSurname());
            this.rolePlayData.setTitle(((User) user).getRolePlaySetting().getTitle());
        } else {
            isNpc = true;
        }
    }

    public int getIdUser() {
        return idUser;
    }
}
