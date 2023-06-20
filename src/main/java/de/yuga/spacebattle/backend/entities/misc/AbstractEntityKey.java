package de.yuga.spacebattle.backend.entities.misc;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class AbstractEntityKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id = -1;

    public AbstractEntityKey() {
    }

    public int getId() {
        return id;
    }
}
