package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;

import javax.annotation.Nullable;

/**
 * The statistics section display.
 */
public class StatsDrawer extends HorizontalLayout {

    public StatsDrawer() {
        setClassName("stats-drawer");
    }

    /**
     * Will updates the statistics section by the given parameter.
     * If null parameter, the section will be fully cleared. Has to be updated with a meaningful behavior.
     *
     * @param component the statistics component to display
     */
    public void setValue(@Nullable final StatisticsDisplay component) {
        removeAll();
        if (component != null) {
            add(component);
        }
    }

}
