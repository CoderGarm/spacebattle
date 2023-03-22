package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EShipClassType;

import javax.annotation.Nonnull;
import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "Armor.getAll", query = "SELECT a FROM Armor a"),
        @NamedQuery(name = "Armor.getAllByResearches",
                query = "SELECT a FROM Armor a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "armor")
@AttributeOverride(name = "id", column = @Column(name = "idArmor"))
public class Armor extends HasCostsByOwn {

    public Armor() {
    }

    public Armor(@Nonnull final NamedTechLevel baseModule,
                 @Nonnull final String technicalTypeName,
                 final int unlockedThroughLevel,
                 final int effectValue,
                 final int tonnage,
                 @Nonnull final EShipClassType shipClassType,
                 @Nonnull final CrewRequirement crewRequirement) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement);
    }
}
