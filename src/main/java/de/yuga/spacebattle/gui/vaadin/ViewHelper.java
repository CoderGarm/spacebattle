package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.server.VaadinServlet;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.Nonnull;

public class ViewHelper {

    private ViewHelper() {
    }

    public static final String MAX_MENU_HEIGHT = "80%";

    public static final String MAX_WIDTH = "80%";

    /**
     * Sets the width, max- and min-width to spacebattle default if with-param is null;
     * Else to the given param.
     *
     * @param component the component to modify
     * @param width     the given width
     * @param <C>       the vaadin components type
     */
    public static <C extends HasSize> void setWidth(final C component, final String width) {
        component.setWidth(width == null ? MAX_WIDTH : width);
        component.setMinWidth(width == null ? MAX_WIDTH : width);
        component.setMaxWidth(width == null ? MAX_WIDTH : width);
    }

    /**
     * Sets the height, max- and min-height to spacebattle default if with-param is null;
     * Else to the given param.
     *
     * @param component the component to modify
     * @param height    the given height
     * @param <C>       the vaadin components type
     */
    public static <C extends HasSize> void setHeight(final C component, @Nonnull final String height) {
        Preconditions.checkNotNull(height, "height shouldn't be null!");

        component.setHeight(height);
        component.setMinHeight(height);
        component.setMaxHeight(height);
    }

    /**
     * Returns a service if the given class type is mapped to a service which is active.
     *
     * @param requiredType the requested services type
     * @param <T>          the class definition of the requested type
     * @return the service
     */
    @Nonnull
    public static <T> T getService(Class<T> requiredType) {
        return WebApplicationContextUtils
                .getWebApplicationContext(VaadinServlet.getCurrent().getServletContext())
                .getBean(requiredType);
    }
}
