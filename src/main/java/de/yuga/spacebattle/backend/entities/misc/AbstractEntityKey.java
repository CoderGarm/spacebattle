package de.yuga.spacebattle.backend.entities.misc;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
