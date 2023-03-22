package de.yuga.spacebattle.misc.fandom.spacecraft.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ClassPhysics {

    @Nonnull
    private Integer masse = null;
    @Nullable
    private Integer laenge = null;
    @Nullable
    private Integer breite = null;
    @Nullable
    private Integer hoehe = null;
    @Nullable
    private Acceleration beschleunigung = null;

    public ClassPhysics(@Nonnull final WikiShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        String rawValue = shipClass.getRawValue(FieldName.Masse);
        if (rawValue != null) {
            rawValue = sanitizeTonnage(rawValue);
            this.masse = Integer.parseInt(rawValue);
        }

        rawValue = shipClass.getRawValue(FieldName.Laenge);
        if (rawValue != null) {
            rawValue = rawValue.replaceAll(",", ".").replaceAll("m", "").replaceAll(" ", "");
            this.laenge = ((Double) Double.parseDouble(rawValue)).intValue();
        }

        rawValue = shipClass.getRawValue(FieldName.Breite);
        if (rawValue != null) {
            rawValue = rawValue.replaceAll(",", ".").replaceAll("m", "").replaceAll(" ", "");
            this.breite = ((Double) Double.parseDouble(rawValue)).intValue();
        }

        rawValue = shipClass.getRawValue(FieldName.Hoehe);
        if (rawValue != null) {
            rawValue = rawValue.replaceAll(",", ".").replaceAll("m", "").replaceAll(" ", "");
            this.hoehe = ((Double) Double.parseDouble(rawValue)).intValue();
        }

        detectAcceleration(shipClass);
    }

    @Nonnull
    private String sanitizeTonnage(final String s) {
        return s.replaceAll(" ", "")
                .replaceAll("t", "")
                .replaceAll("Tonnen", "")
                .replaceAll("Mars-A:", "")
                .replaceAll("\\.", "")
                .replaceAll(",", "")
                ;
    }

    @Nonnull
    public Integer getMasse() {
        return masse;
    }

    @Nullable
    public Integer getLaenge() {
        return laenge;
    }

    @Nullable
    public Integer getBreite() {
        return breite;
    }

    @Nullable
    public Integer getHoehe() {
        return hoehe;
    }

    @Nullable
    public Acceleration getBeschleunigung() {
        return beschleunigung;
    }

    private void detectAcceleration(WikiShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        final List<String> rawValues = shipClass.getRawValues(FieldName.Beschleunigung);
        final Acceleration acceleration = new Acceleration();
        for (String line : rawValues) {
            line = line.replaceAll(",", ".");
            if (line.toLowerCase().contains("Militärischer Schub".toLowerCase())) {
                acceleration.setMaximalBeschleunigung(((Double) Double.parseDouble(line.split(" ")[0])).intValue());
                this.beschleunigung = acceleration;
            }
            if (line.toLowerCase().contains("Vollast".toLowerCase())) {
                acceleration.setVolllastBeschleunigung(((Double) Double.parseDouble(line.split(" ")[0])).intValue());
                this.beschleunigung = acceleration;
            }
        }
    }

    public static class Acceleration {

        @Nullable
        private Integer maximalBeschleunigung = null;
        @Nullable
        private Integer volllastBeschleunigung = null;

        @Nullable
        public Integer getMaximalBeschleunigung() {
            return maximalBeschleunigung;
        }

        public void setMaximalBeschleunigung(@Nullable final Integer maximalBeschleunigung) {
            this.maximalBeschleunigung = maximalBeschleunigung;
        }

        @Nullable
        public Integer getVolllastBeschleunigung() {
            return volllastBeschleunigung;
        }

        public void setVolllastBeschleunigung(@Nullable final Integer volllastBeschleunigung) {
            this.volllastBeschleunigung = volllastBeschleunigung;
        }
    }
}
