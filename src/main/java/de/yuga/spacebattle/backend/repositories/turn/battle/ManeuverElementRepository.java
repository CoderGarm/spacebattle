package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.combat.ManeuverElement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManeuverElementRepository extends JpaRepository<ManeuverElement, Integer> {

}
