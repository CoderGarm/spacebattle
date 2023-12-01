package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.round.CombatRound;
import de.yuga.spacebattle.backend.dto.physics.Time;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class LauncherDescriptor {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The needed time to reload.")
    private Time reloadTime = CombatRound.COMBAT_ROUND;

    public LauncherDescriptor() {
    }

    public LauncherDescriptor(@Nonnull final Launcher content) {
        Preconditions.checkNotNull(content, "content must not be empty");

    }
}
