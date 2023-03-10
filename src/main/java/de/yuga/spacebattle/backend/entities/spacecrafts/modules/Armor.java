package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.misc.HasHullType;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EHullType;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Armor.getAll", query = "SELECT a FROM Armor a"),
        @NamedQuery(name = "Armor.getAllByResearches", query = "SELECT a FROM Armor a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "armor")
@AttributeOverride(name = "id", column = @Column(name = "idArmor"))
public class Armor extends HasHullType {

    public Armor() {
    }

    public Armor(@Nonnull final NamedTechLevel baseModule,
                 @Nonnull final String technicalTypeName,
                 @Nonnull final Research unlockedThrough,
                 final int effectValue,
                 final int costsPercentage,
                 @Nonnull final EHullType hullType) {
        super(baseModule, technicalTypeName, unlockedThrough, costsPercentage, effectValue, hullType);
    }
}
