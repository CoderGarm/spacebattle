package de.yuga.spacebattle.gui.vaadin.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.*;
import de.yuga.spacebattle.gui.vaadin.MainView;

import javax.servlet.http.HttpServletResponse;

@ParentLayout(MainView.class)
//@PreserveOnRefresh
public class NotFoundView extends Div implements HasErrorParameter<NotFoundException> {

    private final Label error = new Label();

    public NotFoundView() {
        add(error);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {

        error.setText("Cannot find URL: " + event.getLocation().getPath());
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
