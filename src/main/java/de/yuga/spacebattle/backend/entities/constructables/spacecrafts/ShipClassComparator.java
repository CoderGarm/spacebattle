package de.yuga.spacebattle.backend.entities.constructables.spacecrafts;

import java.util.Comparator;

/**
 * Sorts the list of ship classes from biggest hull to smallest.
 */
public class ShipClassComparator implements Comparator<ShipClass> {

    @Override
    public int compare(ShipClass o1, ShipClass o2) {
        if (o1 == null || o2 == null || o1.getHull() == null || o2.getHull() == null) {
            return 1;
        }
        if (o1.getHull().getConstructionCapacity() < o2.getHull().getConstructionCapacity()) {
            return 1;
        } else if (o1.getHull().getConstructionCapacity() == o2.getHull().getConstructionCapacity()) {
            return 0;
        } else if (o1.getHull().getConstructionCapacity() > o2.getHull().getConstructionCapacity()) {
            return -1;
        }
        return 0;
    }
}
