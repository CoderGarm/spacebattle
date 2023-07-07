package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.enums.MissionType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MissionType.PIRATE_HUNT)
public class PirateHuntMission extends Mission {
}
