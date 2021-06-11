package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.vaadin.flow.component.select.Select;
import de.yuga.spacebattle.backend.enums.EProductionCategory;

/**
 * A simple vaadin-select for tasks.
 */
public class ProductCategorySelect extends Select<EProductionCategory> {

    public ProductCategorySelect() {
        setLabel("Select a task");
        setTextRenderer(EProductionCategory::name);
    }
}
