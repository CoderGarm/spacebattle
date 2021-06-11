package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.persistence.*;

/**
 * There will be only one propulsion type and it will be taken for FTL- and sub light-travelling.
 * The propulsion is dual-use for both in every ship.
 */
@NamedQueries({
        @NamedQuery(name = "Propulsion.getAll", query = "SELECT a FROM Propulsion a"),
        @NamedQuery(name = "Propulsion.getAllByResearches", query = "SELECT a FROM Propulsion a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "propulsion")
@AttributeOverride(name = "id", column = @Column(name = "idPropulsion"))
public class Propulsion extends BaseModule {

    /**
     * If this propulsion module provides the ability to travel faster than light.
     */
    private boolean ftlCapable = false;

    public Propulsion() {

    }

    public Propulsion(@Nonnull final String name,
                      @Nonnull final String description,
                      @Nonnull final Research unlockedThrough,
                      final int useCapacity,
                      final int effectValue,
                      final int techLevel,
                      final boolean ftlCapable,
                      @Nonnull final CrewRequirementDTO crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement);
        this.ftlCapable = ftlCapable;
    }

    public boolean isFtlCapable() {
        return ftlCapable;
    }
}
