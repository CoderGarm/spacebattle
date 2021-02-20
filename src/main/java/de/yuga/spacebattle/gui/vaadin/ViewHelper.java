package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasSize;

import javax.annotation.Nonnull;

public class ViewHelper {

    private ViewHelper() {
    }

    public static final String MAX_WIDTH = "80%";

    public static <C extends HasSize> void setWidth(final C component, final String width) {
        component.setWidth(width == null ? MAX_WIDTH : width);
        component.setMinWidth(width == null ? MAX_WIDTH : width);
        component.setMaxWidth(width == null ? MAX_WIDTH : width);
    }

    public static <C extends HasSize> void setHeight(final C component, @Nonnull final String height) {
        Preconditions.checkNotNull(height, "height shouldn't be null!");

        component.setHeight(height);
        component.setMinHeight(height);
        component.setMaxHeight(height);
    }
}
