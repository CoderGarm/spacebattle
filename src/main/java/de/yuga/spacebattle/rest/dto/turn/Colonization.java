package de.yuga.spacebattle.rest.dto.turn;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;

public class Colonization {

    @ApiModelProperty(required = true, value = "The id of the colonization.")
    private final int idColonization;

    @Nonnull
    @ApiModelProperty(required = true, value = "The user who runs the colonization.")
    private final UserJson user;

    @Nonnull
    @ApiModelProperty(required = true, value = "The planet which is the target of the colonization.")
    private final Planet target;

    /**
     * Principle: Countdown ticks to zero -> job done.
     * It's about full ticks
     */
    @ApiModelProperty(required = true, value = "The amount of ticks to complete colonization.")
    private final int doneAtZero;

    public Colonization(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Colonization colonization) {
        Preconditions.checkNotNull(colonization, "colonization shouldn't be null!");

        this.idColonization = colonization.getId();
        this.user = new UserJson(colonization.getUser());
        this.target = new Planet(colonization.getTarget());
        this.doneAtZero = colonization.getDoneAtZero();
    }

    public int getIdColonization() {
        return idColonization;
    }

    @Nonnull
    public UserJson getUser() {
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
