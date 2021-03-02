package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomHullRepository {

    @Nonnull
    List<Hull> findAll();

    @Nonnull
    List<Hull> findAllByUser(User user);
}
