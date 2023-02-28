package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(description = ".")
public class UserReq {

    @Nonnull
    @JsonProperty
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @Schema(required = true, description = "The user's name")
    private String username;

    @Nonnull
    @JsonProperty
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    @Schema(required = true, description = "The user's password - only shipped in a creation process")
    private String password;

    @Email
    @Nonnull
    @JsonProperty
    @Size(min = 1, max = 50)
    @Schema(required = true, description = "The user's e-mail - only shipped in a creation process")
    private String email;

    @JsonProperty
    @Schema(required = true, description = "The user don't want to provide an eMail and will not recover the password.")
    private Boolean noEMailWanted = false;

    public UserReq() {
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

    public boolean isNoEMailWanted() {
        return noEMailWanted;
    }

    public User transform() {
        return new User(username, password, email, EWebUserRole.USER, noEMailWanted);
    }
}
