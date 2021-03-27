package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

import javax.annotation.Nullable;

/**
 * The statistics section display.
 */
@CssImport("./styles/views/main/details/StatsDrawer.css")
public class StatsDrawer extends HorizontalLayout {

    public StatsDrawer() {
        ViewHelper.setHeight(this, "100%");
        setClassName("stats-drawer");
    }

    /**
     * Will updates the statistics section by the given parameter.
     * If null parameter, the section will be fully cleared. Has to be updated with a meaningful behavior. todo
     *
     * @param components the components to display
     */
    public void update(@Nullable final Component... components) {
        removeAll();
        if (components != null) {
            add(components);
        }
    }

}
