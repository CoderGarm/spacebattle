package de.yuga.spacebattle.gui.vaadin;

import com.vaadin.flow.component.HasSize;

public class ViewHelper {

    private ViewHelper() {
    }

    public static final String MAX_WIDTH = "80%";

    public static <C extends HasSize> void setWidth(final C component, final String width) {
        component.setWidth(width == null ? MAX_WIDTH : width);
        component.setMinWidth(width == null ? MAX_WIDTH : width);
        component.setMaxWidth(width == null ? MAX_WIDTH : width);
    }
}
