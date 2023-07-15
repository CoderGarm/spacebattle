package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;

/**
 * This indicated what a type of ship is this hull for.
 */
public enum EShipClassType implements HasIconName {

    // Warships
    LAC("LAC", false, false, "fighter", "light attack craft (11 to 21 thousand tons, no hyper capability)"),
    VT("VT", false, false, "corvette", "corvette"),
    FG("FG", false, false, "cruiser", "frigate"),
    DD("DD", false, false, "corvette", "destroyer (68 to 189 thousand tons)"),
    CL("CL", false, false, "frigate", "light cruiser (88 to 147 thousand tons)"),
    CA("CA", false, false, "frigate", "heavy cruiser (228 to 483 thousand tons)"),
    BC("BC", false, false, "frigate", "battlecruiser (780 thousand to 2.5 million tons)"),
    BCP("BC(P)", true, false, "frigate", "Battlecruiser pod-layer (1.7 million to 1.8 million tons)"),
    BB("BB", false, false, "frigate", "battleship (2 to 4 million tons)"),
    DN("DN", false, false, "frigate", "dreadnought (5 to 6.5 million tons)"),
    CLAC("CLAC", false, false, "frigate", "LAC carrier (6.2 million tons)"),
    SD("SD", false, false, "frigate", "super dreadnought (7 to 9 million tons)"),
    SDP("SD(P)", true, false, "frigate", "super dreadnought pod-layer (8.5 to 8.7 million tons)"),

    //Auxiliary Warship
    AE("AE", false, true, "satellite", "ammunition ship"),
    AR("AR", false, true, "satellite", "repair ship"),
    FAT("FAT", false, true, "satellite", "fast attack transport"),
    FR("FR", false, true, "satellite", "freighter"),
    ;

    private static final Set<EShipClassType> CIVIL = Set.of(FR);

    @Nonnull
    private final String type;

    private final boolean podLayer;

    private final boolean auxiliaryShip;

    @Nonnull
    final String iconName;

    @Nonnull
    private final String description;

    EShipClassType(@Nonnull final String type,
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
    public static EShipClassType valueOf(@Nonnull final de.yuga.spacebattle.rest.dto.enums.EShipClassType shipClassType) {
        Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");

        return Arrays.stream(values()).filter(ht -> ht.getType().equals(shipClassType.getType())).findFirst().orElseThrow(NullPointerException::new);
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

    public boolean isCivilShip() {
        return CIVIL.contains(this);
    }

    public boolean suitsShipClassType(@Nonnull final EShipClassType comparator) {
        // todo make it better
        return ordinal() >= comparator.ordinal();
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    /**
     * States the impact of this class for a given mission.<br>
     * That means
     */
    public int getHeatImpact(@Nonnull final EMissionType missionType) {
        Preconditions.checkNotNull(missionType, "missionType must not be empty");

        switch (missionType) {
            case PIRATE_RAID:

                if (isCivilShip()) {
                    // I am prey
                    return 3;
                }
                if (isAuxiliaryShip()) {
                    // I am smaller prey
                    return 1;
                }
                // I am reducing heat by fighting pirates
                return -ordinal();
            default:
            case PIRATE_HUNT:
            case CONVOY_PROTECTION:
                throw new NotifyWebUserException("In this state I am supposed to be only used to state my impact as part of a counter mission.");
        }
    }
}
