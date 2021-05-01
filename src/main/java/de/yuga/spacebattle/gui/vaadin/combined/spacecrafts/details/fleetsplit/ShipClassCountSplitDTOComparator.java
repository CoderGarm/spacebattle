package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClassComparator;

import java.util.Comparator;

/**
 * Compares ship class sowieso dto on the key level with the same method that the ship classes will be compared.
 */
public class ShipClassCountSplitDTOComparator implements Comparator<ShipClassCountSplitDTO> {
    @Override
    public int compare(ShipClassCountSplitDTO o1, ShipClassCountSplitDTO o2) {
        return new ShipClassComparator().compare(o1.getShipClass(), o2.getShipClass());
    }
}
