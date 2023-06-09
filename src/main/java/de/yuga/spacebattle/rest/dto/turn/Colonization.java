package de.yuga.spacebattle.rest.dto.turn;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Colonization {

    @Schema(required = true, description = "The id of the colonization.")
    private int idColonization;

    @Nonnull
    @Schema(required = true, description = "The user who runs the colonization.")
    private Player user;

    @Nonnull
    @Schema(required = true, description = "The planet which is the target of the colonization.")
    private Planet target;

    /**
     * Principle: Countdown ticks to zero -> job done.
     * It's about full ticks
     */
    @Schema(required = true, description = "The amount of ticks to complete colonization.")
    private int doneAtZero;

    public Colonization() {
    }

    public Colonization(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");

        this.idColonization = colonization.getId();
        this.user = new Player(colonization.getUser());
        this.target = new Planet(colonization.getTarget());
        this.doneAtZero = colonization.getDoneAtZero();
    }

    public int getIdColonization() {
        return idColonization;
    }

    @Nonnull
    public Player getUser() {
        return user;
    }

    @Nonnull
    public Planet getTarget() {
        return target;
    }

    public int getDoneAtZero() {
        return doneAtZero;
    }
}
