package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import jakarta.persistence.*;

import javax.annotation.Nonnull;

@NamedQueries({
        @NamedQuery(name = "ElectronicWarfare.getAll", query = "SELECT a FROM ElectronicWarfare a"),
        @NamedQuery(name = "ElectronicWarfare.getAllByResearches",
                query = "SELECT a FROM ElectronicWarfare a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "electronicWarfare")
@AttributeOverride(name = "id", column = @Column(name = "idElectronicWarfare"))
public class ElectronicWarfare extends HasCostsByOwn {

    /**
     * Defines the range of this eloka.
     */
    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance effectiveRange;

    public ElectronicWarfare() {
    }

    public ElectronicWarfare(@Nonnull final NamedTechLevel baseModule,
                             @Nonnull final String technicalTypeName,
                             final int unlockedThroughLevel,
                             final int effectValue,
                             final int tonnage,
                             @Nonnull final EShipClassType shipClassType,
                             @Nonnull final CrewRequirement crewRequirement,
                             @Nonnull final Distance effectiveRange) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, effectValue, tonnage, shipClassType, crewRequirement);
        Preconditions.checkNotNull(effectiveRange, "effectiveRange shouldn't be null!");

        this.effectiveRange = effectiveRange;
    }

    @Nonnull
    public Distance getEffectiveRange() {
        return effectiveRange;
    }
}
