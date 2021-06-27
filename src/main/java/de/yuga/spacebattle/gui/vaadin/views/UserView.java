package de.yuga.spacebattle.gui.vaadin.views;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.misc.PageWithActionTabs;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Shows the information about users and user messages.
 */
@SpringComponent
@UIScope
@Route(value = UserView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = UserView.ROUTE, layout = MainView.class)
public class UserView extends PageWithActionTabs<VerticalLayout> {

    public static final String ROUTE = "user";

    @Nonnull
    private VerticalLayout content;

    @Nonnull
    private final VerticalLayout userListDisplay;

    @Nonnull
    private final VerticalLayout userMessageDisplay;

    @Autowired
    public UserView(@Nonnull final UserService userService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        if (userService.getLoggedInUser() == null) {
            throw new NotifySBUserException("You need to be logged in!");
        }
        addClassName("user-view");

        userListDisplay = new UserListDisplay(userService);
        userMessageDisplay = new UserMessagesDisplay(userService);
        createActionSelectorMenu();

        content = setContent(userListDisplay);
    }

    @Override
    protected void createActionSelectorMenu() {
        final Tab userList = new Tab("User list");
        userList.setEnabled(true);
        addComponentForTabOfActionMenu(userList, userListDisplay);

        final Tab userMessages = new Tab("User messages");
        userMessages.setEnabled(true);
        addComponentForTabOfActionMenu(userMessages, userMessageDisplay);

        addActionListener();
    }

    @Override
    protected void updateActionMenuUsability(@Nullable Map<Tab, Boolean> readOnlyMap) {
        /* no op */
    }

    @Override
    protected void addActionListener() {
        actionSelectorMenu.addSelectedChangeListener(event -> {
            final Tab selectedTab = event.getSelectedTab();
            final VerticalLayout componentForTab = getComponentForTabOfActionMenu(selectedTab);
            content = setContent(componentForTab);
        });
    }
}
