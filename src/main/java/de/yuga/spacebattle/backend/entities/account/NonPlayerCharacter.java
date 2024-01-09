package de.yuga.spacebattle.backend.entities.account;

import de.yuga.spacebattle.backend.enums.OwnerType;

import javax.annotation.Nonnull;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(OwnerType.NPC)
public class NonPlayerCharacter extends Owner {

    public NonPlayerCharacter() {
    }

    public NonPlayerCharacter(@Nonnull final String username, @Nonnull final String shipPrefix) {
        super(username);

        this.getRolePlaySetting().setShipPrefix(shipPrefix);
    }
}
