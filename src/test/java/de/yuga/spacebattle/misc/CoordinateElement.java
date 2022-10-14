package de.yuga.spacebattle.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.platform.commons.util.StringUtils;

public class CoordinateElement {

    public static final String NOPE = "NOPE";
    private String id;
    private String name;
    private final Position position;
    private Owner owner;

    public CoordinateElement(final CoordinateElement coordinateElement, final Position position) {
        this.id = coordinateElement.getId();
        this.name = coordinateElement.getName();
        this.position = position;
        this.owner = coordinateElement.getOwner();
    }

    public CoordinateElement(final String[] split) {
        this.id = NOPE;
        this.name = split[0];
        this.position = new Position(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        this.owner = new Owner();
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @JsonIgnore
    public int getDistance(final CoordinateElement that) {
        return position.getDistance(that.getPosition());
    }

    @JsonIgnore
    public boolean isNope() {
        return id.isBlank() || id.equals(NOPE);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public void invertYAxis() {
        position.invertYAxis();
    }

    @JsonIgnore
    public int getXCoord() {
        return position.getX();
    }

    @JsonIgnore
    public int getYCoord() {
        return position.getY();
    }

    @Override
    public String toString() {
        return (StringUtils.isNotBlank(name) ? name : id) + ", " + position.toString();
    }

    @JsonIgnore
    public boolean isCandidate(final CoordinateElement reference,
                               final int referenceDistance,
                               final double referenceBearingTo,
                               final double deviance) {

        final double minFactor = 1 - deviance;
        final double maxFactor = 1 + deviance;
        final int minDist = (int) (referenceDistance * minFactor);
        final int maxDist = (int) (referenceDistance * maxFactor);

        final double minBearing = referenceBearingTo * minFactor;
        final double maxBearing = referenceBearingTo * maxFactor;


        final int myDist = reference.getPosition().getDistance(position);
        final double myBearingTo = reference.getPosition().bearingTo(position);

        return (minDist <= myDist && maxDist >= myDist) && (minBearing <= myBearingTo && maxBearing >= myBearingTo);
    }
}
