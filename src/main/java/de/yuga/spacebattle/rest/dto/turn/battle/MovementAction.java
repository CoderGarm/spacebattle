package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class MovementAction {

    @Nullable
    @Schema(required = true, description = "The round and phase information.")
    private de.yuga.spacebattle.rest.dto.turn.battle.CombatRoundKey combatRoundKey;

    @Nullable
    @Schema(required = true, description = "The fleet which acts.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet actor;

    @Nullable
    @Schema(required = true, description = "The selected movement option for this action.")
    private EMovementType movementType;

    @Nullable
    @Schema(required = true, description = "The starting position for this movement.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit origin;

    @Nullable
    @Schema(required = true, description = "The next step to the targeted position.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit interimDestination;

    @Nullable
    @Schema(required = true, description = "The point of the targeted position.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit destination;

    public MovementAction() {
    }

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction input,
                          @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor(), languageCode);
        this.movementType = input.getMovementType();
        this.origin = new Orbit(input.getOrigin());
        this.interimDestination = new Orbit(input.getInterimDestination());
        this.destination = new Orbit(input.getDestination());
    }

    @Nullable
    public CombatRoundKey getCombatRoundKey() {
        return combatRoundKey;
    }

    public void setCombatRoundKey(@Nullable final CombatRoundKey combatRoundKey) {
        this.combatRoundKey = combatRoundKey;
    }

    @Nullable
    public Fleet getActor() {
        return actor;
    }

    public void setActor(@Nullable final Fleet actor) {
        this.actor = actor;
    }

    @Nullable
    public EMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(@Nullable final EMovementType movementType) {
        this.movementType = movementType;
    }

    @Nullable
    public Orbit getOrigin() {
        return origin;
    }

    public void setOrigin(@Nullable final Orbit origin) {
        this.origin = origin;
    }

    @Nullable
    public Orbit getInterimDestination() {
        return interimDestination;
    }

    public void setInterimDestination(@Nullable final Orbit interimDestination) {
        this.interimDestination = interimDestination;
    }

    @Nullable
    public Orbit getDestination() {
        return destination;
    }

    public void setDestination(@Nullable final Orbit destination) {
        this.destination = destination;
    }
}
