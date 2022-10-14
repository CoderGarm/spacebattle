package de.yuga.spacebattle.misc;

public class CoordinateDifference {

    private final CoordinateElement fromCsv;
    private final CoordinateElement fromJson;

    public CoordinateDifference(final CoordinateElement fromCsv, final CoordinateElement fromJson) {
        this.fromCsv = fromCsv;
        this.fromJson = fromJson;
    }

    public CoordinateElement getFromCsv() {
        return fromCsv;
    }

    public CoordinateElement getFromJson() {
        return fromJson;
    }

    public int getDistance() {
        return fromCsv.getPosition().getDistance(fromJson.getPosition());
    }

    @Override
    public String toString() {
        return fromCsv.getName()
                + ", fromCsv " + fromCsv.getPosition().toString()
                + ", fromJson " + fromJson.getPosition().toString()
                + ", distance: " + fromCsv.getPosition().getDistance(fromJson.getPosition());
    }
}
