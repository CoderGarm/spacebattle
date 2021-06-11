package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.vaadin.flow.component.select.Select;
import de.yuga.spacebattle.backend.enums.EResourceType;

/**
 * A simple vaadin-select for resources.
 */
public class ResourceTypeSelect extends Select<EResourceType> {

    public ResourceTypeSelect() {
        setLabel("Select a resource");
        setTextRenderer(EResourceType::getSingularName);
    }
}
