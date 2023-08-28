package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.enums.ECollectableType.*;

public enum EResourceType implements HasIconName {

    CONSTRUCTION(FORFEITABLE),
    ORBITAL_CONSTRUCTION(FORFEITABLE),
    RESEARCH(FORFEITABLE),
    CREDITS(COLLECTABLE),
    METALORE(COLLECTABLE),
    RARE_ELEMENTS(COLLECTABLE),
    HEAVY_METALS(COLLECTABLE),
    POPULATION(VIABLE),
    ;

    @Nonnull
    final String iconName;

    @Nonnull
    private final ECollectableType collectableType;

    EResourceType(@Nonnull final ECollectableType collectableType) {
        Preconditions.checkNotNull(collectableType, "collectableType shouldn't be null!");

        this.collectableType = collectableType;
        this.iconName = this.name();
    }

    @Nonnull
    public ECollectableType getCollectableType() {
        return collectableType;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }

    /**
     * Returns all the values of {@link EResourceType} except {@link #POPULATION}.
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
        return Arrays.stream(EResourceType.values()).filter(e -> FORFEITABLE == e.getCollectableType()).toArray(EResourceType[]::new);
    }

    public static boolean isCollectable(@Nonnull final EResourceType resourceType) {
        Preconditions.checkNotNull(resourceType, "resourceType must not be empty");

        return Arrays.stream(valuesWhichAreCollectable()).collect(Collectors.toSet()).contains(resourceType);
    }
}
