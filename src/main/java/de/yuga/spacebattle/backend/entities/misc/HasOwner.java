package de.yuga.spacebattle.backend.entities.misc;

import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nullable;

public interface HasOwner {

    @Nullable
    Owner getOwner();

    @Nullable
    User getHumanOwner();

    @Nullable
    NonPlayerCharacter getNpcOwner();
}
