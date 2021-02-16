package de.yuga.spacebattle.gui.vaadin.misc;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;

@SpringComponent
//@PreserveOnRefresh
@UIScope
@Route(value = InfoView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = InfoView.ROUTE, layout = MainView.class)
public class InfoView extends VerticalLayout {

    public static final String ROUTE = "info";

    public InfoView() {

        H1 h1 = new H1(new Text("Lorem ipsum"));
        add(h1);
        Text text1 = new Text("Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.");
        add(text1);
        H2 h2 = new H2(new Text("Ipsum lorem"));
        add(h2);
        Text text2 = new Text("Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.");
        add(text2);
        // shorthand methods for changing the component theme variants
        setPadding(false);
        setMargin(true);
        // just a demonstration of the API, by default the spacing is on
        setSpacing(true);
        ViewHelper.setWidth(this, null);
    }
}
