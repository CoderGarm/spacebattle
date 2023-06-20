package de.yuga.spacebattle.backend.entities.misc;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class Deletable extends AbstractEntityKey {

    /**
     * Marks if the class is deleted.
     */
    @Column(columnDefinition = "boolean not null default false")
    private boolean isDeleted = false;

    public Deletable() {
    }

    public void delete() {
        isDeleted = true;
    }

    public void animate() {
        isDeleted = false;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isAlive() {
        return !isDeleted;
    }
}
