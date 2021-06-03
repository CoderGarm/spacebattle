package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Colonization;

import java.util.List;

public interface CustomColonizationRepository {

    List<Colonization> findAll();

    List<Colonization> findAllForUser(User user);
}
