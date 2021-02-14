package de.yuga.spacebattle.gui.vaadin.account.dashboard;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.account.details.UserDisplay;
import de.yuga.spacebattle.gui.vaadin.combined.account.AllianceDisplay;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplayMulti;
import de.yuga.spacebattle.gui.vaadin.research.ResearchDisplayMulti;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@SpringComponent
//@PreserveOnRefresh
@UIScope
@Route(value = DashboardView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = DashboardView.ROUTE, layout = MainView.class)
public class DashboardView extends HorizontalLayout {

    public static final String ROUTE = "dash";

    @Nonnull
    private final UserService userService;

    @Autowired
    public DashboardView(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.userService = userService;
        User user = this.userService.isLoggedIn();
        if (user == null) {
            throw new NotifySBUserException("You are empty!");
        }
        UserDisplay userDisplay = new UserDisplay(user);
        add(userDisplay);

        Alliance alliance = user.getAlliance(); //warum leer?
        if (alliance != null) {
            add(new AllianceDisplay(alliance));
        }

        add(new PlanetDisplayMulti(user.getOwnedPlanets()));
        add(new ResearchDisplayMulti(user.getResearches().keySet()));


        addClassName("user-view");
    }

}
