package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.physics.Mass;
import de.yuga.spacebattle.rest.dto.WithCosts;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Missile extends WithCosts<Missile> {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The type name of this missile.")
    private BaseModule baseModule;

    @JsonProperty
    @Schema(required = true, description = "The engine capacity of this missile.")
    private int elokaResistance;

    @JsonProperty
    @Schema(required = true, description = "The uses capacity of this missile.")
    private Mass tonnage;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The warhead of this missile.")
    private de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Warhead warhead;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The implemented engine of this missile.")
    private MissileMotor missileMotor;

    public Missile() {
    }

    public Missile(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile missile,
                   @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(missile, "missile shouldn't be null!");

        this.baseModule = new BaseModule(missile, languageCode);
        this.elokaResistance = missile.getElokaResistance();
        this.tonnage = missile.getTonnage();
        this.warhead = new Warhead(missile.getWarhead(), languageCode);
        this.missileMotor = new MissileMotor(missile.getMissileMotor(), languageCode);
    }

    @JsonIgnore
    public int getIdMissile() {
        return baseModule.getIdModule();
    }
}
