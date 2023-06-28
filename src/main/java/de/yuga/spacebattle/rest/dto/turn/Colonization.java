package de.yuga.spacebattle.rest.dto.turn;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class Colonization {

    @JsonProperty
    @Schema(required = true, description = "The id of the colonization.")
    private int idColonization;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The user who runs the colonization.")
    private Player user;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet which is the target of the colonization.")
    private Planet target;

    /**
     * Principle: Countdown ticks to zero -> job done.
     * It's about full ticks
     */
    @JsonProperty
    @Schema(required = true, description = "The amount of ticks to complete colonization.")
    private int doneAtZero;

    @JsonProperty
    @Schema(required = true, description = "If the colonization is planned and not started.")
    private boolean isPlanned;

    public Colonization() {
    }

    public Colonization(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");

        this.idColonization = colonization.getId();
        this.user = new Player(colonization.getUser());
        this.target = new Planet(colonization.getTarget());
        this.doneAtZero = colonization.getDoneAtZero();
        this.isPlanned = colonization.isPlanned();
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
