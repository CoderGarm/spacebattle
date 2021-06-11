package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "ElectronicWarfare.getAll", query = "SELECT a FROM ElectronicWarfare a"),
        @NamedQuery(name = "ElectronicWarfare.getAllByResearches", query = "SELECT a FROM ElectronicWarfare a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "electronicWarfare")
@AttributeOverride(name = "id", column = @Column(name = "idElectronicWarfare"))
public class ElectronicWarfare extends BaseModule {

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final String name,
                             @Nonnull final String description,
                             @Nonnull final Research unlockedThrough,
                             final int useCapacity,
                             final int effectValue,
                             final int techLevel,
                             @Nonnull final CrewRequirementDTO crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement);
    }
}
