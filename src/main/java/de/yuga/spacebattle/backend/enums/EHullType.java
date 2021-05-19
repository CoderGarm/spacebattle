package de.yuga.spacebattle.backend.enums;

import javax.annotation.Nonnull;

/**
 * This indicated what a type of ship is this hull for.
 */
public enum EHullType {

    // Warships
    LAC("LAC", false, false, "light attack craft (11 to 21 thousand tons, no hyper capability)"),
    FG("FG", false, false, "frigate"),
    DD("DD", false, false, "destroyer (68 to 189 thousand tons)"),
    CC("CC", false, false, "cruiser in general"),
    CL("CL", false, false, "light cruiser (88 to 147 thousand tons)"),
    CA("CA", false, false, "heavy cruiser (228 to 483 thousand tons)"),
    BC("CA", false, false, "battlecruiser (780 thousand to 2.5 million tons)"),
    BCP("BC(P)", true, false, "Battlecruiser pod-layer (1.7 million to 1.8 million tons)"),
    BB("BB", false, false, "battleship (2 to 4 million tons)"),
    DN("DN", false, false, "dreadnought (5 to 6.5 million tons)"),
    CLAC("CLAC", false, false, "LAC carrier (6.2 million tons)"),
    SD("SD", false, false, "super dreadnought (7 to 9 million tons)"),
    SDP("SD(P)", false, false, "super dreadnought pod-layer (8.5 to 8.7 million tons)"),

    //Auxiliary Warship
    AE("AE", false, true, "ammunition ship"),
    AR("AR", false, true, "repair ship"),
    FAT("FAT", false, true, "fast attack transport"),
    FR("FR", false, true, "freighter"),
    ;

    @Nonnull
    private final String type;

    private final boolean podLayer;

    private final boolean auxiliaryShip;

    @Nonnull
    private final String description;

    EHullType(@Nonnull final String type, boolean podLayer, final boolean auxiliaryShip, @Nonnull final String description) {
        this.type = type;
        this.podLayer = podLayer;
        this.auxiliaryShip = auxiliaryShip;
        this.description = description;
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
    public String getDescription() {
        return description;
    }
}
