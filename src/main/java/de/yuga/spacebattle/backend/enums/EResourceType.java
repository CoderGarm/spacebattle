package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static de.yuga.spacebattle.backend.enums.ECollectableType.*;

public enum EResourceType implements HasIconName {

    CONSTRUCTION(FORFEITABLE, "Construction Point", "Construction Points", "construction"),
    ORBITAL_CONSTRUCTION(FORFEITABLE, "Shipyard Construction Point", "Shipyard Construction Points", "orbitalconstruction"),
    RESEARCH(FORFEITABLE, "Research Point", "Research Point", "research"),
    CREDITS(COLLECTABLE, "Credit", "Credits", "credit"),
    METALORE(COLLECTABLE, "Metalore", "Metalore", "metalore"),
    RARE_ELEMENTS(COLLECTABLE, "Rare elements", "Rare elements", "mercurium"),
    HEAVY_METALS(COLLECTABLE, "Heavy metal", "Heavy metals", "hyperonium"),
    POPULATION(VIABLE, "Population", "Population", "population"),
    ;

    @Nonnull
    private final String singularName;

    @Nonnull
    private final String pluralName;

    @Nonnull
    final String iconName;

    @Nonnull
    private final ECollectableType collectableType;

    EResourceType(@Nonnull final ECollectableType collectableType,
                  @Nonnull final String singularName,
                  @Nonnull final String pluralName,
                  @Nonnull final String iconName) {
        Preconditions.checkNotNull(collectableType, "collectableType shouldn't be null!");
        Preconditions.checkNotNull(singularName, "singularName shouldn't be null!");
        Preconditions.checkNotNull(pluralName, "pluralName shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.collectableType = collectableType;
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.iconName = iconName;
    }

    @Nonnull
    public ECollectableType getCollectableType() {
        return collectableType;
    }

    @Nonnull
    public String getSingularName() {
        return singularName;
    }

    @Nonnull
    public String getPluralName() {
        return pluralName;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }

    /**
     * Returns all the the values of {@link EResourceType} except {@link #POPULATION}.
     *
     * @return the values
     */
    public static EResourceType[] valuesWithoutPopulation() {
        return Arrays.stream(EResourceType.values()).filter(e -> EResourceType.POPULATION != e).toArray(EResourceType[]::new);
    }

    public static EResourceType[] valuesWhichAreCollectable() {
        return Arrays.stream(EResourceType.values()).filter(e -> COLLECTABLE == e.getCollectableType()).toArray(EResourceType[]::new);
    }

    public static EResourceType[] valuesWhichForfeits() {
        return Arrays.stream(EResourceType.values()).filter(e -> FORFEITABLE != e.getCollectableType()).toArray(EResourceType[]::new);
    }
}
