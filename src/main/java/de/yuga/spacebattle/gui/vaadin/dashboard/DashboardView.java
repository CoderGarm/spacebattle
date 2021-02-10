package de.yuga.spacebattle.gui.vaadin.dashboard;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.account.details.UserDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplay;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SpringComponent
@PreserveOnRefresh
@UIScope
@Route(value = DashboardView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = DashboardView.ROUTE, layout = MainView.class)
public class DashboardView extends HorizontalLayout {

    public static final String ROUTE = "dash";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final H4 planetsTitle = new H4("Planets");

    @Autowired
    public DashboardView(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.userService = userService;


        addClassName("user-view");

        User user = userService.isLoggedIn();
        UserDisplay userDisplay = new UserDisplay(user);
        add(userDisplay);

        add(planetsTitle);
        List<PlanetDisplay> planetDisplays = new ArrayList<>();
        user.getOwnedPlanets().forEach(planet -> {
            PlanetDisplay planetDisplay = new PlanetDisplay(planet);
            planetDisplays.add(planetDisplay);
            add(planetDisplay);
        });

    }

}
