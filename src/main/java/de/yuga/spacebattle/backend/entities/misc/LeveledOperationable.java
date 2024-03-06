package de.yuga.spacebattle.backend.entities.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class LeveledOperationable extends AbstractEntityKey {

    private int level = 0;

    private int operationalLevel = 0;

    /**
     * Marks if the class is operational and hold its crew.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isOperational = false;

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idTickActivated", referencedColumnName = "idTick")
    private Tick activated;

    public LeveledOperationable() {
    }


    public int getOperationalLevel() {
        return operationalLevel;
    }

    public void setLevel(final int level) {
        if (level <= this.level) {
            throw new NotifyWebUserException("You cannot reduce the level of a construction");
        }
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setOperationalLevel(final int operationalLevel, @Nonnull final Tick today) {
        this.activated = Preconditions.checkNotNull(today, "today must not be empty");
        this.operationalLevel = operationalLevel;
    }

    public void setOperational(@Nonnull final Tick today) {
        this.activated = Preconditions.checkNotNull(today, "today must not be empty");
        this.isOperational = true;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public void setInoperational() {
        this.isOperational = false;
        this.activated = null;
    }
}
