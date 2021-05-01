package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClassComparator;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ShipClassCountDTO;

import java.util.Comparator;

/**
 * Compares ship class ... dto on the key level with the same method that the ship classes will be compared.
 */
public class ShipClassCountDTOComparator implements Comparator<ShipClassCountDTO> {

    @Override
    public int compare(ShipClassCountDTO o1, ShipClassCountDTO o2) {
        return new ShipClassComparator().compare(o1.getShipClass(), o2.getShipClass());
    }
}
