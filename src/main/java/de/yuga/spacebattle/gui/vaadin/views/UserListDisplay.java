package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.account.details.UserDisplay;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Renders a table with all existing users.
 */
public class UserListDisplay extends VerticalLayout {

    UserListDisplay(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService);

        final Div userList = new Div();
        final List<User> allUsers = userService.findAll();
        for (User user : allUsers) {
            UserDisplay userDisplay = new UserDisplay(user);
            userList.add(userDisplay);
        }
        add(userList);
    }
}
