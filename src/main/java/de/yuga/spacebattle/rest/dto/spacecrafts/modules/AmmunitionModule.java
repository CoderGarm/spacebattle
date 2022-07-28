package de.yuga.spacebattle.rest.dto.spacecrafts.modules;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class AmmunitionModule {

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @Schema(required = true, description = "The basic values of this module.")
    private Missile missile;

    public AmmunitionModule() {
    }

    public AmmunitionModule(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule ammunitionModule,
                            @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(ammunitionModule, "ammunitionModule shouldn't be null!");

        this.baseModule = new BaseModule(ammunitionModule, languageCode);
        this.missile = new Missile(ammunitionModule.getMissile(), languageCode);
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(@Nonnull BaseModule baseModule) {
        this.baseModule = baseModule;
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }

    public void setMissile(@Nonnull Missile missile) {
        this.missile = missile;
    }
}
