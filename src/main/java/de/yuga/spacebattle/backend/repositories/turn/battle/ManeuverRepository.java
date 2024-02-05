package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.combat.Maneuver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManeuverRepository extends JpaRepository<Maneuver, Integer> {

}
