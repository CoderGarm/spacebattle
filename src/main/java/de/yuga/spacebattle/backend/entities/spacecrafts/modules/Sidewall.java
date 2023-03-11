package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.entities.misc.HasHullType;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EHullType;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Sidewall.getAll", query = "SELECT a FROM Sidewall a"),
        @NamedQuery(name = "Sidewall.getAllByResearches",
                query = "SELECT a FROM Sidewall a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "sidewall")
@AttributeOverride(name = "id", column = @Column(name = "idSidewall"))
public class Sidewall extends HasHullType {

    public Sidewall() {
    }

    public Sidewall(@Nonnull final NamedTechLevel baseModule,
                    @Nonnull final String technicalTypeName,
                    final int unlockedThroughLevel,
                    final int effectValue,
                    final int costsPercentage,
                    @Nonnull final EHullType hullType) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, costsPercentage, hullType);
    }
}
