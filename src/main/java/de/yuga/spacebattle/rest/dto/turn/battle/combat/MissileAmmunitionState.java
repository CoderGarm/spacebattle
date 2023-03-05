package de.yuga.spacebattle.rest.dto.turn.battle.combat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.fittings.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateAccessor;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.AmmunitionValue;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = ".")
public class MissileAmmunitionState {

    /**
     * The amount of remaining missile in the arsenal of a warship.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The amount of missiles.")
    private final List<AmmunitionValue> shotsPerMissile = new ArrayList<>();

    public MissileAmmunitionState() {
    }

    public MissileAmmunitionState(@Nonnull final WarshipHealthStateAccessor healthState, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(healthState, "healthState must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        shotsPerMissile.addAll(healthState.getRemainingShots().entrySet().stream()
                .map(e -> new AmmunitionValue(e.getKey(), e.getValue(), languageCode))
                .collect(Collectors.toList()));
    }

    public MissileAmmunitionState(@Nonnull final Set<AmmunitionFitting> ammunitionFittings, @Nonnull final String languageCode) {
        Preconditions.checkNotNull(ammunitionFittings, "ammunitionFittings must not be empty");
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");

        final Map<de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile, Integer> shotsPerMissile = new HashMap<>();
        ammunitionFittings.forEach(f -> {
            final AmmunitionModule ammunitionModule = f.getAmmunitionModule();
            final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile missile = ammunitionModule.getMissile();
            final int effectValue = ammunitionModule.getEffectValue();
            final int amountOfModules = f.getAmount();
            shotsPerMissile.merge(missile, amountOfModules * effectValue, Integer::sum);
        });

        this.shotsPerMissile.addAll(shotsPerMissile.entrySet().stream()
                .map(e -> new AmmunitionValue(e.getKey(), e.getValue(), languageCode))
                .collect(Collectors.toList()));
    }
}
