package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Armor.getAll", query = "SELECT a FROM Armor a"),
        @NamedQuery(name = "Armor.getAllByResearches", query = "SELECT a FROM Armor a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "armor")
@AttributeOverride(name = "id", column = @Column(name = "idArmor"))
public class Armor extends BaseModule {

    public Armor() {
    }

    public Armor(@Nonnull final String name,
                 @Nonnull final String description,
                 @Nonnull final Research unlockedThrough,
                 final int useCapacity,
                 final int effectValue,
                 final int techLevel) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel);
    }
}
