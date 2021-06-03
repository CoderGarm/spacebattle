package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

import java.util.Set;

/**
 * Simply a transfer object to ask a star system for some properties by double-colon operator.
 */
public class StarSystemColonizationDTO {

    private final StarSystem starSystem;

    private final Set<Planet> planets;

    /**
     * If the system is known by the logged in user.
     */
    private final boolean isKnown;

    public StarSystemColonizationDTO(StarSystem starSystem, boolean isKnown) {
        this.starSystem = starSystem;
        this.planets = starSystem.getPlanets();
        this.isKnown = isKnown;
    }

    public StarSystem getStarSystem() {
        return starSystem;
    }

    public Set<Planet> getPlanets() {
        return planets;
    }

    public String getStarSystemName() {
        return starSystem.getName();
    }

    public Orbit getStarSystemOrbit() {
        return starSystem.getOrbit();
    }

    public boolean isKnown() {
        return isKnown;
    }
}
