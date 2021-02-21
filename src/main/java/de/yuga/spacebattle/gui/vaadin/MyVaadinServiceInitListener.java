package de.yuga.spacebattle.gui.vaadin;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import de.yuga.spacebattle.NotifySBUserException;

@SpringComponent
public class MyVaadinServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(e -> {
            e.getSession().setErrorHandler(errorEvent -> {
                Throwable t = errorEvent.getThrowable();
                Dialog dialog = new Dialog();
                //dialog.setWidth("650px");
                //dialog.setHeight("150pc");
                dialog.add(new Label(t.getMessage()));
                dialog.setOpened(true);
                if (!(t instanceof NotifySBUserException)) {
                    t.printStackTrace();
                }
            });
        });
    }
}
