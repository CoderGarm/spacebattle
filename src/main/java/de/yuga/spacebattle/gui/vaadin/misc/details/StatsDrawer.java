package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

public class StatsDrawer extends HorizontalLayout {

    public StatsDrawer() {
        ViewHelper.setHeight(this, "100%");
        setClassName("stats-drawer");
    }

    public void update(Component... components) {
        removeAll();
        add(components);
    }

}
