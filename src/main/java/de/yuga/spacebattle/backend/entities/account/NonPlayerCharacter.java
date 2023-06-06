package de.yuga.spacebattle.backend.entities.account;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("NPC")
public class NonPlayerCharacter extends Owner {

    public NonPlayerCharacter() {
    }
}
