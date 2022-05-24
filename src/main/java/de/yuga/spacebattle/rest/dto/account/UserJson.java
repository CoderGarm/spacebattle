package de.yuga.spacebattle.rest.dto.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.SpacebattleApplication;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * The simplest representation of a user.
 * Think about registering new classes in {@link SpacebattleApplication#api()}.
 */
@Schema(description = ".")
public class UserJson {

    @Nullable
    @Schema(required = true, description = "The user's database id.")
    private Integer idUser;

    @Nullable
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @Schema(required = true, description = "The user's name")
    private String username;

    @Nullable
    @Schema(description = "The user's alliance.")
    private Integer idAlliance;

    @Nonnull
    @Schema(required = true, description = "The user's role.")
    private final String role = EWebUserRole.USER.getName();

    public UserJson() {
    }

    public UserJson(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.idUser = user.getId();
        this.username = user.getUsername();
        this.idAlliance = user.getAlliance() != null ? user.getAlliance().getId() : null;
    }

    @Nullable
    public Integer getIdUser() {
        return idUser;
    }

    @Nonnull
    public String getUsername() {
        return Objects.requireNonNull(username);
    }

    @Nullable
    public Integer getIdAlliance() {
        return idAlliance;
    }

    @Nonnull
    public String getRole() {
        return role;
    }
}
