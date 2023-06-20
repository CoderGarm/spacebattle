package de.yuga.spacebattle.backend.entities.account;

import de.yuga.spacebattle.backend.enums.OwnerType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import javax.annotation.Nonnull;

@Entity
@DiscriminatorValue(OwnerType.NPC)
public class NonPlayerCharacter extends Owner {

    public NonPlayerCharacter() {
    }

    public NonPlayerCharacter(@Nonnull final String username) {
        super(username);
    }
}
