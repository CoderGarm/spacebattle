package de.yuga.spacebattle.backend.entities.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EModuleType;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.enums.ESupportType;
import de.yuga.spacebattle.backend.enums.ETechLevel;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "orbitalModule")
@AttributeOverride(name = "id", column = @Column(name = "idOrbitalModule"))
public class OrbitalModule extends HasCosts {

    private int baseValue;

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private ESupportType effect;

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    private int unlockedThroughLevel;

    public OrbitalModule() {
    }

    public OrbitalModule(@Nonnull final String name,
                         @Nonnull final String description,
                         final int tonnage,
                         final int baseValue,
                         @Nonnull final CrewRequirement crewRequirement,
                         @Nonnull final ETechLevel techLevel,
                         @Nonnull final EModuleType effect,
                         @Nonnull final Research unlockedThrough,
                         final int unlockedThroughLevel) { // fixme check tonnage and price
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, tonnage, OrbitalModule.class);
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(effect, "effect shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.baseValue = baseValue;
        this.getCosts().setCrewRequirement(crewRequirement);
        this.effect = Objects.requireNonNull(ESupportType.getByValue(effect));
        this.unlockedThrough = unlockedThrough;
        this.unlockedThroughLevel = unlockedThroughLevel;
    }

    public OrbitalModule(@Nonnull final String name,
                         @Nonnull final String description,
                         final int tonnage,
                         final int baseValue,
                         @Nonnull final CrewRequirement crewRequirement,
                         @Nonnull final ETechLevel techLevel,
                         @Nonnull final EResourceType effect,
                         @Nonnull final Research unlockedThrough,
                         final int unlockedThroughLevel) { // fixme check tonnage and price
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, tonnage, OrbitalModule.class);
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(effect, "effect shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        this.baseValue = baseValue;
        this.getCosts().setCrewRequirement(crewRequirement);
        this.effect = Objects.requireNonNull(ESupportType.getByValue(effect));
        this.unlockedThrough = unlockedThrough;
        this.unlockedThroughLevel = unlockedThroughLevel;
    }

    public int getBaseValue() {
        return baseValue;
    }

    @Nonnull
    public ESupportType getEffect() {
        return effect;
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    public int getUnlockedThroughLevel() {
        return unlockedThroughLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrbitalModule)) return false;

        OrbitalModule building = (OrbitalModule) o;

        return getId() == building.getId();
    }

}
