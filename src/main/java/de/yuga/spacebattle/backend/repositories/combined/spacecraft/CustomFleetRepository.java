package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;

import java.util.List;

public interface CustomFleetRepository {

    List<Fleet> findAllFleets();

    List<Fleet> findAllFleetsBy(User user);
}
