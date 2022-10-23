package de.yuga.spacebattle.backend.entities.misc;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class Deletable extends AbstractEntityKey {

    /**
     * Marks if the class is deleted.
     */
    @Column(columnDefinition = "bit not null default false")
    private boolean isDeleted = false;

    public Deletable() {
    }

    public void setDeleted() {
        isDeleted = true;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isAlive() {
        return !isDeleted;
    }
}
