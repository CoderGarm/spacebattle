package de.yuga.spacebattle.rest.dto.account;

import de.yuga.spacebattle.SpacebattleApplication;
import de.yuga.spacebattle.backend.entities.account.User;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * The simplest representation of a user.
 * Think about registering new classes in {@link SpacebattleApplication#api()}.
 */
public class UserReq {

    @Nullable
    @ApiModelProperty("The user's database id.")
    private Integer idUser;

    @Nonnull
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @ApiModelProperty(required = true, value = "The user's name")
    private String username;

    @Nonnull
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    @ApiModelProperty(required = true, value = "The user's password - only shipped in a creation process")
    private String password;

    @Nonnull
    @Size(min = 1, max = 50)
    @Email
    @ApiModelProperty(required = true, value = "The user's e-mail - only shipped in a creation process")
    private String email;

    @Nullable
    private Integer idAlliance;

    @Nullable
    public Integer getIdUser() {
        return idUser;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    @Nullable
    public Integer getIdAlliance() {
        return idAlliance;
    }

    public User transform() {
        return new User(username, password, email);
    }
}
