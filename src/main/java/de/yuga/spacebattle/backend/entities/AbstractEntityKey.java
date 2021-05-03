package de.yuga.spacebattle.backend.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Simply the entity key.
 */
@MappedSuperclass
public class AbstractEntityKey implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id = -1;

    public AbstractEntityKey() {
    }

    public int getId() {
        return id;
    }

    /**
     * Heavily fucked up method. Use it only if you have no other way.
     *
     * @param id the id
     */
    public void setId(final int id) {
        this.id = id;
    }
}
