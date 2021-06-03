package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import de.yuga.spacebattle.backend.entities.orbitals.Planet;

import javax.annotation.Nullable;

/**
 * The base for all 'select a planet' transfer objects related to colonization.
 */
public class ColonizePlanetSelectionDTO {

    @Nullable
    private Planet colonizationSelection;

    /**
     * Just a unnecessary constructor to separate fields from methods.
     */
    public ColonizePlanetSelectionDTO() {
    }

    @Nullable
    public Planet getColonizationSelection() {
        return colonizationSelection;
    }

    public void setColonizationSelection(@Nullable final Planet colonizationSelection) {
        this.colonizationSelection = colonizationSelection;
    }
}
