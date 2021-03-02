package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.account.details.UserDisplay;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SpringComponent
@UIScope
@Route(value = UserView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = UserView.ROUTE, layout = MainView.class)
public class UserView extends HorizontalLayout {

    public static final String ROUTE = "user";

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final List<UserDisplay> userDisplays = new ArrayList<>();

    @Autowired
    public UserView(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        addClassName("user-view");
        this.userService = userService;
        List<User> all = userService.findAll();
        for (User user : all) {
            UserDisplay userDisplay = new UserDisplay(user);
            userDisplays.add(userDisplay);
            add(userDisplay);
        }
        UserDisplay[] userDisplays = this.userDisplays.toArray(UserDisplay[]::new);
        //setVerticalComponentAlignment(Alignment.STRETCH, userDisplays);
    }

}
