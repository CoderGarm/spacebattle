package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * This indicated what a type of ship is this hull for.
 */
public enum EHullType {

    // Warships
    LAC("LAC", false, false, "fighter", "light attack craft (11 to 21 thousand tons, no hyper capability)"),
    FG("FG", false, false, "cruiser", "frigate"),
    DD("DD", false, false, "corvette", "destroyer (68 to 189 thousand tons)"),
    CC("CC", false, false, "frigate", "cruiser in general"),
    CL("CL", false, false, "frigate", "light cruiser (88 to 147 thousand tons)"),
    CA("CA", false, false, "frigate", "heavy cruiser (228 to 483 thousand tons)"),
    BC("CA", false, false, "frigate", "battlecruiser (780 thousand to 2.5 million tons)"),
    BCP("BC(P)", true, false, "frigate", "Battlecruiser pod-layer (1.7 million to 1.8 million tons)"),
    BB("BB", false, false, "frigate", "battleship (2 to 4 million tons)"),
    DN("DN", false, false, "frigate", "dreadnought (5 to 6.5 million tons)"),
    CLAC("CLAC", false, false, "frigate", "LAC carrier (6.2 million tons)"),
    SD("SD", false, false, "frigate", "super dreadnought (7 to 9 million tons)"),
    SDP("SD(P)", false, false, "frigate", "super dreadnought pod-layer (8.5 to 8.7 million tons)"),

    //Auxiliary Warship
    AE("AE", false, true, "satellite", "ammunition ship"),
    AR("AR", false, true, "satellite", "repair ship"),
    FAT("FAT", false, true, "satellite", "fast attack transport"),
    FR("FR", false, true, "satellite", "freighter"),
    ;

    @Nonnull
    private final String type;

    private final boolean podLayer;

    private final boolean auxiliaryShip;

    @Nonnull
    final String iconName;

    @Nonnull
    private final String description;

    EHullType(@Nonnull final String type,
              boolean podLayer,
              final boolean auxiliaryShip,
              @Nonnull final String iconName,
              @Nonnull final String description) {
        Preconditions.checkNotNull(type, "type shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.type = type;
        this.podLayer = podLayer;
        this.auxiliaryShip = auxiliaryShip;
        this.description = description;
        this.iconName = iconName;
    }

    @Nonnull
    public String getType() {
        return type;
    }

    public boolean isPodLayer() {
        return podLayer;
    }

    public boolean isAuxiliaryShip() {
        return auxiliaryShip;
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }
}
