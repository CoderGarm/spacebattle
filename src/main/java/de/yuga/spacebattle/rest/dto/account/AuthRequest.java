package de.yuga.spacebattle.rest.dto.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Schema(description = ".")
public class AuthRequest {

    @Nonnull
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    private String username;

    @Nonnull
    @NotNull
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    private String password;

    public AuthRequest() {
    }

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

    @Override
    public String toString() {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        final AuthRequest clone = new AuthRequest(username, "******");
        return gson.toJson(clone);
    }
}
