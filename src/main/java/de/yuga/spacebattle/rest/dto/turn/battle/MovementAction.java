package de.yuga.spacebattle.rest.dto.turn.battle;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.enums.EMovementType;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.orbitals.Orbit;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MovementAction {

    @Nullable
    @ApiModelProperty(required = true, value = "The round and phase information.")
    private de.yuga.spacebattle.rest.dto.turn.battle.CombatRoundKey combatRoundKey;

    @Nullable
    @ApiModelProperty(required = true, value = "The fleet which acts.")
    private de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet actor;

    @Nullable
    @ApiModelProperty(required = true, value = "The selected movement option for this action.")
    private EMovementType movementType;

    @Nullable
    @ApiModelProperty(required = true, value = "The starting position for this movement.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit origin;

    @Nullable
    @ApiModelProperty(required = true, value = "The next step to the targeted position.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit interimDestination;

    @Nullable
    @ApiModelProperty(required = true, value = "The point of the targeted position.")
    private de.yuga.spacebattle.rest.dto.orbitals.Orbit destination;

    public MovementAction(@Nonnull final de.yuga.spacebattle.backend.entities.turn.battle.combat.MovementAction input) {
        Preconditions.checkNotNull(input, "input shouldn't be null!");

        this.combatRoundKey = new CombatRoundKey(input.getId(), input.getCombatRound(), input.getCombatPhase());
        this.actor = new Fleet(input.getActor());
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
