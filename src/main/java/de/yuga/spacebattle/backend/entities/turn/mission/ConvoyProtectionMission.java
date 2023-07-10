package de.yuga.spacebattle.backend.entities.turn.mission;

import de.yuga.spacebattle.backend.enums.MissionType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MissionType.CONVOY_PROTECTION)
public class ConvoyProtectionMission extends Mission {

    /* fixme how to map a single convoy to a warship?
    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idMissile", referencedColumnName = "idMissile")
    @Column(name = "amount", columnDefinition = "decimal(19, 0)", nullable = false)
    @CollectionTable(name = "remainingShotsSnapshot", joinColumns = @JoinColumn(name = "idWarshipHealthStateSnapshot"))
    private final Map<TradedResource, Integer> remainingShots = new HashMap<>();
    */
}
