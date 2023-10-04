package de.yuga.spacebattle.backend.entities.misc;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class Operationable extends Deletable {

    /**
     * Marks if the class is operational and hold its crew.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isOperational = false;

    public Operationable() {
    }

    public void setOperational() {
        isOperational = true;
    }

    public void setOperational(final boolean isOperational) {
        this.isOperational = isOperational;
    }

    public boolean isOperational() {
        return isOperational;
    }

    public boolean isOperationalFromSuper() {
        return isOperational;
    }
}
