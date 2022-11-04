package de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.i18n.Translation;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.enums.ETechLevel;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public class BaseModule extends HasCosts {

    @Nonnull
    @NotNull
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idResearch")
    private Research unlockedThrough;

    /**
     * The capacity represents the capacity in metric tons which will be occupied if build in.<br>
     * This capacity includes all 'opportunity costs' to use a module, from crew quarters up to toilets, from screens and displays up to seats and impact cages.
     */
    @NotNull
    private int useCapacity;

    /**
     * Which is the targeted ship's hull class.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EHullType hullType;

    protected BaseModule() {
    }

    public BaseModule(@Nonnull final String name,
                      @Nonnull final String description,
                      @Nonnull final Research unlockedThrough,
                      final int useCapacity,
                      @Nonnull final EHullType hullType,
                      @Nonnull final ETechLevel techLevel,
                      @Nonnull final CrewRequirement crewRequirement,
                      @Nonnull final Class<?> clazz) {
        super(new Translation(Translation.DEFAULT_LANGUAGE, name), new Translation(Translation.DEFAULT_LANGUAGE, description), techLevel, useCapacity, clazz);
        Preconditions.checkNotNull(hullType, "hullType must not be empty");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(crewRequirement, "crewRequirement shouldn't be null!");

        this.unlockedThrough = unlockedThrough;
        this.useCapacity = useCapacity;
        this.hullType = hullType;
        this.getCosts().setCrewRequirement(crewRequirement);
    }

    @Nonnull
    public Research getUnlockedThrough() {
        return unlockedThrough;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    @Nonnull
    public EHullType getHullType() {
        return hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setHullType(@Nonnull final EHullType hullType) {
        this.hullType = hullType;
    }

    @Deprecated(since = MasterOfTheUniverseService.BALANCING_ISSUES)
    public void setUseCapacity(final int useCapacity) {
        this.useCapacity = useCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseModule)) return false;

        BaseModule module = (BaseModule) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
