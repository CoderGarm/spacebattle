package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Sidewall.getAll", query = "SELECT a FROM Sidewall a"),
        @NamedQuery(name = "Sidewall.getAllByResearches", query = "SELECT a FROM Sidewall a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "sidewall")
@AttributeOverride(name = "id", column = @Column(name = "idSidewall"))
public class Sidewall extends BaseModule {

    public Sidewall() {
    }

    public Sidewall(@Nonnull final String name,
                    @Nonnull final String description,
                    @Nonnull final Research unlockedThrough,
                    final int useCapacity,
                    final int effectValue,
                    final int techLevel) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel);
    }
}
