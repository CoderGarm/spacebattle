package de.yuga.spacebattle.rest.dto.spacecrafts.modules;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics.BaseModule;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Launcher {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The basic values of this module.")
    private BaseModule baseModule;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The way how the damage will be projected.")
    private EWeaponType weaponType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The bunch of allowed missiles for this launcher.")
    private List<de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile> allowedMissiles = new ArrayList<>();

    public Launcher() {
    }

    public Launcher(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher launcher,
                    @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(launcher, "launcher shouldn't be null!");

        this.baseModule = new BaseModule(launcher, languageCode);
        this.weaponType = launcher.getWeaponType();
        this.allowedMissiles = launcher.getAllowedMissiles().stream()
                .map(m -> new de.yuga.spacebattle.rest.dto.spacecrafts.ammunition.Missile(m, languageCode))
                .collect(Collectors.toList());
    }

    @Nonnull
    public BaseModule getBaseModule() {
        return baseModule;
    }
}
