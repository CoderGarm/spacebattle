package de.yuga.spacebattle.gui.vaadin.misc;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.MainView;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.account.CreateAccountDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;


@SpringComponent
@UIScope
@Route(value = LoginView.ROUTE, layout = MainView.class)
@CssImport("./styles/views/account/user-view.css")
@RouteAlias(value = LoginView.ROUTE, layout = MainView.class)
public class LoginView extends VerticalLayout {

    @Nonnull
    public static final String ROUTE = "login";

    @Nonnull
    private final static Logger LOGGER = LoggerFactory.getLogger(LoginView.class);

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Nonnull
    private final Button createUser = createUserButton();

    @Nonnull
    private final CreateAccountDialog createAccountDialog;

    @Autowired
    public LoginView(@Nonnull final UserService userService,
                     @Nonnull final EventBus.UIEventBus uiEventBus,
                     @Nonnull final CreateAccountDialog createAccountDialog) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");
        Preconditions.checkNotNull(createAccountDialog, "createAccountDialog shouldn't be null!");

        this.userService = userService;
        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.createAccountDialog = createAccountDialog;

        addClassName("login-view");

        add(createUser);

        add(new H1(new Text("Lorem ipsum")));
        add(new Text("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
        add(new H2(new Text("Ipsum lorem")));
        add(new Text("Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.   "));
        setPadding(false);
        setMargin(true);
        setSpacing(true);
        ViewHelper.setWidth(this, null);


    }

    @Nonnull
    private Button createUserButton() {
        return new Button("Create your account", e -> {
            createAccountDialog.open();
        });
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }


}
