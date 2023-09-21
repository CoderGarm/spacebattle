package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.EStarNation;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * The simplest representation of a user.
 */
@Schema(description = ".")
public class RolePlayData {

    @Nullable
    @JsonProperty
    @Size(min = 3, max = 50)
    private String title;

    @Nullable
    @JsonProperty
    @Size(min = 3, max = 8)
    private String titleAbbreviation;

    @Nullable
    @JsonProperty
    @Size(min = 3, max = 50)
    private String firstname;

    @Nullable
    @JsonProperty
    @Size(min = 3, max = 50)
    private String surname;

    @Nullable
    @JsonProperty
    @Size(min = 3, max = 6)
    @Schema(description = "The user's selected ship names.")
    private String shipPrefix;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's selected ship name templates.")
    private Set<EStarNation> shipNameTemplates = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's selected ship names.")
    private Set<String> shipNames = new HashSet<>();

    public RolePlayData() {
    }

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    public void setTitleAbbreviation(@Nullable final String titleAbbreviation) {
        this.titleAbbreviation = titleAbbreviation;
    }

    public void setFirstname(@Nullable final String firstname) {
        this.firstname = firstname;
    }

    public void setSurname(@Nullable final String surname) {
        this.surname = surname;
    }

    public void setShipPrefix(@Nullable final String shipPrefix) {
        this.shipPrefix = shipPrefix;
    }

    public void setShipNameTemplates(@Nonnull final Set<EStarNation> shipNameTemplates) {
        this.shipNameTemplates = shipNameTemplates;
    }

    public void setShipNames(@Nonnull final Set<String> shipNames) {
        this.shipNames = shipNames;
    }
}
