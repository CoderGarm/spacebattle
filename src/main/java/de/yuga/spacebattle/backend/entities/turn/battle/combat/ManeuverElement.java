package de.yuga.spacebattle.backend.entities.turn.battle.combat;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.enums.physics.EDistanceMetric;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "maneuverElement")
@AttributeOverride(name = "id", column = @Column(name = "idManeuverElement"))
public class ManeuverElement extends AbstractEntityKey {

    @NotNull
    @Nonnull
    @ManyToOne
    @JoinColumn(name = "idManeuver")
    private Maneuver maneuver;

    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordinateP1"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordinateP1"))
    private Orbit p1;

    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordinateCP1"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordinateCP1"))
    private Orbit cp1;

    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordinateCP2"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordinateCP2"))
    private Orbit cp2;

    @NotNull
    @Nonnull
    @Embedded
    @AttributeOverride(name = "xCoordinate", column = @Column(name = "xCoordinateP2"))
    @AttributeOverride(name = "yCoordinate", column = @Column(name = "yCoordinateP2"))
    private Orbit p2;

    /**
     * The number of planned execution for the complete maneuver.
     */
    private int sequenceNo;

    public ManeuverElement() {
    }

    public ManeuverElement(@Nonnull final Maneuver maneuver,
                           @Nonnull final de.yuga.spacebattle.backend.combat.maneuver.ManeuverElement maneuverElement) {
        Preconditions.checkNotNull(maneuverElement, "maneuverElement must not be empty");

        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
        final CubicBezier curve = maneuverElement.getCurve();
        this.p1 = getOrbit(curve.getP1());
        this.cp1 = getOrbit(curve.getCp1());
        this.cp2 = getOrbit(curve.getCp2());
        this.p2 = getOrbit(curve.getP2());
        this.sequenceNo = maneuverElement.getSequenceNo();
    }

    @Nonnull
    public CubicBezier getCubicBezier() {
        return new CubicBezier(
                getPoint(p1),
                getPoint(cp1),
                getPoint(cp2),
                getPoint(p2)
        );
    }

    private double[] getPoint(@Nonnull final Orbit position) {
        Preconditions.checkNotNull(position, "position must not be empty");

        final double x = position.getXCoordinate().getCoordinateInMetric(EDistanceMetric.KM).doubleValue();
        final double y = position.getYCoordinate().getCoordinateInMetric(EDistanceMetric.KM).doubleValue();
        return new double[]{x, y};
    }

    @Nonnull
    private Orbit getOrbit(final double[] position) {
        Preconditions.checkArgument(position.length == 2, "position.length == 2 must be empty");

        final double x = position[0];
        final double y = position[1];
        return new Orbit(x, y, EDistanceMetric.KM);
    }

    @Nonnull
    public Maneuver getManeuver() {
        return maneuver;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    @Nonnull
    public Orbit getP1() {
        return p1;
    }

    @Nonnull
    public Orbit getCp1() {
        return cp1;
    }

    @Nonnull
    public Orbit getCp2() {
        return cp2;
    }

    @Nonnull
    public Orbit getP2() {
        return p2;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ManeuverElement that = (ManeuverElement) o;

        return new EqualsBuilder().append(sequenceNo, that.sequenceNo).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(sequenceNo).toHashCode();
    }

    public void setManeuver(@Nonnull final Maneuver maneuver) {
        this.maneuver = Preconditions.checkNotNull(maneuver, "maneuver must not be empty");
    }
}
