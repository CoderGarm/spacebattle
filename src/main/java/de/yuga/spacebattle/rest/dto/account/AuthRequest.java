package de.yuga.spacebattle.rest.dto.account;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class AuthRequest {

    @Nonnull
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    private final String username;

    @Nonnull
    @NotNull
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    private final String password;

    public AuthRequest(@Nonnull final String username,
                       @Nonnull final String password) {
        this.username = username;
        this.password = password;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }
}
