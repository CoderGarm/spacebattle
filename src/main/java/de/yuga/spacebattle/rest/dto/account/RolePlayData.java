package de.yuga.spacebattle.rest.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.EmbassyTextBlocks;
import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import de.yuga.spacebattle.backend.enums.EStarNation;
import de.yuga.spacebattle.backend.enums.events.EGameEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
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

    @Nullable
    @JsonProperty
    @Size(max = 50)
    @Schema(description = "The empire's name.")
    private String empireName;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's selected ship name templates.")
    private Set<EStarNation> shipNameTemplates = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's selected ship names.")
    private Set<String> shipNames = new HashSet<>();

    @Valid
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user's selected ship names.")
    private RPGTextBlocks textBlocks = new RPGTextBlocks();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The participation.")
    private Set<EGameEvent> participant = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The event wins.")
    private Set<EGameEvent> winner = new HashSet<>();

    public RolePlayData() {
    }

    public RolePlayData(@Nonnull final RolePlaySetting rolePlaySetting) {
        Preconditions.checkNotNull(rolePlaySetting, "rolePlaySetting must not be empty");

        this.title = rolePlaySetting.getTitle();
        this.titleAbbreviation = rolePlaySetting.getTitleAbbreviation();
        this.firstname = rolePlaySetting.getFirstname();
        this.surname = rolePlaySetting.getSurname();
        this.empireName = rolePlaySetting.getEmpireName();

        final EmbassyTextBlocks embassyTextBlocks = rolePlaySetting.getEmbassyTextBlocks();
        this.textBlocks.setLeftUpper(embassyTextBlocks.getLeftUpper());
        this.textBlocks.setRightUpper(embassyTextBlocks.getRightUpper());
        this.textBlocks.setLeftBottom(embassyTextBlocks.getLeftBottom());
        this.textBlocks.setRightBottom(embassyTextBlocks.getRightBottom());

        this.participant.addAll(rolePlaySetting.getParticipant());
        this.winner.addAll(rolePlaySetting.getWinner());
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

    public void setShipNameTemplates(@Nonnull final Set<EStarNation> shipNameTemplates) {
        this.shipNameTemplates = shipNameTemplates;
    }


    @Nullable
    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(@Nullable final String empireName) {
        this.empireName = empireName;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getTitleAbbreviation() {
        return titleAbbreviation;
    }

    @Nullable
    public String getFirstname() {
        return firstname;
    }

    @Nullable
    public String getSurname() {
        return surname;
    }

    @Nullable
    public String getShipPrefix() {
        return shipPrefix;
    }

}
