package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;

public class ManeuverElement implements Cloneable, Comparable<ManeuverElement> {

    @Nonnull
    private final CubicBezier curve;

    /**
     * Represented as percent value.
     */
    private int partOfManeuver;

    /**
     * The number of planned execution for the complete maneuver.
     */
    private int sequenceNo;

    public ManeuverElement(@Nonnull final CubicBezier curve, final int partOfManeuver, final int sequenceNo) {
        this.curve = Preconditions.checkNotNull(curve, "curve must not be empty");
        this.partOfManeuver = partOfManeuver;
        this.sequenceNo = sequenceNo;
    }

    @Nonnull
    public CubicBezier getCurve() {
        return curve;
    }

    public int getPartOfManeuver() {
        return partOfManeuver;
    }

    public void setPartOfManeuver(final int partOfManeuver) {
        this.partOfManeuver = partOfManeuver;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ManeuverElement that = (ManeuverElement) o;

        return new EqualsBuilder().append(curve, that.curve).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(curve).toHashCode();
    }

    @Override
    public ManeuverElement clone() {
        try {
            //noinspection UnnecessaryLocalVariable
            final ManeuverElement clone = (ManeuverElement) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(@Nonnull final ManeuverElement o) {
        Preconditions.checkNotNull(o, "o must not be empty");

        return Integer.compare(getSequenceNo(), o.getSequenceNo());
    }

    public void increaseSequenceNo() {
        sequenceNo++;
    }
}
