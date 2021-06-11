package de.yuga.spacebattle.gui.vaadin;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import de.yuga.spacebattle.NotifySBUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringComponent
public class MyVaadinServiceInitListener implements VaadinServiceInitListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyVaadinServiceInitListener.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> {
            e.getSession().setErrorHandler(errorEvent -> {
                final Throwable t = errorEvent.getThrowable();
                final Dialog dialog = new Dialog();
                dialog.add(new Label(t.getMessage()));
                dialog.setOpened(true);
                if (!(t instanceof NotifySBUserException)) {
                    t.printStackTrace();
                }
                LOGGER.warn(t.getMessage());
            });
        });
    }
}
