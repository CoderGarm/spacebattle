package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@NamedQueries({
        @NamedQuery(name = "AmmunitionModule.getAll", query = "SELECT a FROM AmmunitionModule a"),
        @NamedQuery(name = "AmmunitionModule.getAllByResearches", query = "SELECT a FROM AmmunitionModule a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "ammunitionModule")
@AttributeOverride(name = "id", column = @Column(name = "idAmmunitionModule"))
public class AmmunitionModule extends BaseModuleWithEffectValue {

    /**
     * The missile type where this ammunition is for.
     */
    @Nonnull
    @NotNull
    @OneToOne(mappedBy = "ammunitionModule")
    @JoinColumn(name = "idMissile")
    private Missile missile;

    public AmmunitionModule() {
    }

    public AmmunitionModule(@Nonnull final String name,
                            @Nonnull final String description,
                            @Nonnull final Research unlockedThrough,
                            final int useCapacity,
                            final int effectValue,
                            @Nonnull final ETechLevel techLevel,
                            @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement, AmmunitionModule.class);
    }

    @Nonnull
    public Missile getMissile() {
        return missile;
    }
}
