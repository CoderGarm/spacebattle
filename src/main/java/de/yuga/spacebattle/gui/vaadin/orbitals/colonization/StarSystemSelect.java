package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.vaadin.flow.component.select.Select;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;

/**
 * A simple vaadin-select for star systems
 */
public class StarSystemSelect extends Select<StarSystem> {

    public StarSystemSelect() {
        setLabel("Select a system");
        setTextRenderer(StarSystem::getName);
    }
}
