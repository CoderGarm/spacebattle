package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "AmmunitionModule.getAll", query = "SELECT a FROM AmmunitionModule a"),
        @NamedQuery(name = "AmmunitionModule.getAllByResearches", query = "SELECT a FROM AmmunitionModule a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "ammunitionModule")
@AttributeOverride(name = "id", column = @Column(name = "idAmmunitionModule"))
public class AmmunitionModule extends BaseModule {

    /**
     * Defines what kind of property is supported.
     */
    @Nullable
    @OneToOne(mappedBy = "ammunitionModule", optional = false)
    @JoinColumn(name = "idWeapon")
    private Weapon weapon;

    public AmmunitionModule() {
    }

    public AmmunitionModule(@Nonnull final String name,
                            @Nonnull final String description,
                            @Nonnull final Research unlockedThrough,
                            final int useCapacity,
                            final int effectValue,
                            final int techLevel) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel);
    }

    @Nullable
    public Weapon getWeapon() {
        return weapon;
    }

    public void setWeapon(@Nonnull Weapon weapon) {
        this.weapon = weapon;
    }
}
