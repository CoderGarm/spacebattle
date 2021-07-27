package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EStarClassType implements HasIconName {

    CLASS_O("O", 49.60, "class_O_star"),
    CLASS_B("B", 33.42, "class_O_star"),
    CLASS_A("A", 28.76, "class_O_star"),
    CLASS_F0("F0", 25.42, "class_O_star"),
    CLASS_F1("F1", 25.98, "class_O_star"),
    CLASS_F2("F2", 25.54, "class_O_star"),
    CLASS_F3("F3", 25.10, "class_O_star"),
    CLASS_F4("F4", 24.66, "class_O_star"),
    CLASS_F5("F5", 24.10, "class_O_star"),
    CLASS_F6("F6", 23.76, "class_O_star"),
    CLASS_F7("F7", 23.32, "class_O_star"),
    CLASS_F8("F8", 22.88, "class_O_star"),
    CLASS_F9("F9", 22.44, "class_O_star"),
    CLASS_G0("G0", 22.00, "class_O_star"),
    CLASS_G1("G1", 21.56, "class_O_star"),
    CLASS_G2("G2", 21.12, "class_O_star"),
    CLASS_G3("G3", 20.68, "class_O_star"),
    CLASS_G4("G4", 20.24, "class_O_star"),
    CLASS_G5("G5", 19.80, "class_O_star"),
    CLASS_G6("G6", 19.36, "class_O_star"),
    CLASS_G7("G7", 18.92, "class_O_star"),
    CLASS_G8("G8", 18.48, "class_O_star"),
    CLASS_G9("G9", 18.04, "class_O_star"),
    CLASS_K0("K0", 17.60, "class_O_star"),
    CLASS_K1("K1", 17.15, "class_O_star"),
    CLASS_K2("K2", 16.72, "class_O_star"),
    CLASS_K3("K3", 16.28, "class_O_star"),
    CLASS_K4("K4", 15.84, "class_O_star"),
    CLASS_K5("K5", 15.40, "class_O_star"),
    CLASS_K6("K6", 14.96, "class_O_star"),
    CLASS_K7("K7", 14.52, "class_O_star"),
    CLASS_K8("K8", 14.08, "class_O_star"),
    CLASS_K9("K9", 13.64, "class_O_star"),
    CLASS_M0("M0", 13.20, "class_O_star"),
    CLASS_M1("M1", 12.76, "class_O_star"),
    CLASS_M2("M2", 12.32, "class_O_star"),
    CLASS_M3("M3", 11.88, "class_O_star"),
    CLASS_M4("M4", 11.44, "class_O_star"),
    CLASS_M5("M5", 11.00, "class_O_star"),
    CLASS_M6("M6", 10.56, "class_O_star"),
    CLASS_M7("M7", 10.12, "class_O_star"),
    CLASS_M8("M8", 9.68, "class_O_star"),
    CLASS_M9("M9", 9.24, "class_O_star"),
    CLASS_RG("Red Giant", 5.64, "class_O_star"),
    ;

    /**
     * Spectral class	Limit in light minutes
     */
    @Nonnull
    final String spectralClass;

    final double lightMinutesToHyperLimit;

    @Nonnull
    final String iconName;

    EStarClassType(@Nonnull final String spectralClass,
                   final double lightMinutesToHyperLimit,
                   @Nonnull final String iconName) {
        Preconditions.checkNotNull(spectralClass, "name shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.spectralClass = spectralClass;
        this.lightMinutesToHyperLimit = lightMinutesToHyperLimit;
        this.iconName = iconName;
    }

    @Nonnull
    public String getSpectralClass() {
        return spectralClass;
    }

    public double getLightMinutesToHyperLimit() {
        return lightMinutesToHyperLimit;
    }

    @Nonnull
    @Override
    public String getIconName() {
        return iconName;
    }
}
