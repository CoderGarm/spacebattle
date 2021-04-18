package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NotificationHelper {

    private NotificationHelper() {
    }

    /**
     * Opens a notification with the given text.
     *
     * @param text     the text to display
     * @param duration the duration in milliseconds after the notification should disappear
     */
    public static void notify(@Nonnull final String text, @Nullable final Integer duration) {
        Preconditions.checkNotNull(text, "text shouldn't be null!");

        final Span span = new Span(text);
        NotificationHelper.notify(span, duration);
    }

    /**
     * Opens a notification with the given text.
     *
     * @param component the Component to display
     * @param duration  the duration in milliseconds after the notification should disappear
     */
    public static void notify(@Nonnull final Component component, @Nullable final Integer duration) {
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        final NativeButton buttonInside = new NativeButton("Close");
        final Notification notification = new Notification(component, buttonInside);
        buttonInside.addClickListener(event -> notification.close());
        notification.setPosition(Notification.Position.MIDDLE);
        if (duration != null) {
            notification.setDuration(duration);
        }
        notification.open();
    }
}
