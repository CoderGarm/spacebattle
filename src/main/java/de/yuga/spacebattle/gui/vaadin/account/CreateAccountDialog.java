package de.yuga.spacebattle.gui.vaadin.account;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.gui.vaadin.NotificationHelper;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.account.details.UserEditService;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import de.yuga.spacebattle.gui.vaadin.views.DashboardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;

@SpringComponent
@UIScope
public class CreateAccountDialog extends Dialog {

    @Nonnull
    private final UserEditService userEditService;

    @Nonnull
    private final Button close;

    @Nonnull
    private final Button submit;

    @Nonnull
    private final UserService userService;

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Autowired
    public CreateAccountDialog(@Nonnull final UserService userService,
                               @Nonnull final EventBus.UIEventBus uiEventBus,
                               @Nonnull final UserEditService userEditService) {
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");
        Preconditions.checkNotNull(userEditService, "userEdit shouldn't be null!");

        this.userService = userService;
        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);
        this.userEditService = userEditService;

        setModal(true);
        ViewHelper.setWidth(userEditService, "200px");
        ViewHelper.setHeight(userEditService, "400px");

        this.close = new Button("Close", c -> {
            this.close();
        });
        this.submit = new Button("Submit", s -> {
            UserEditService.UserObject user = userEditService.getUser();

            String username = user.getUsername();
            String email = user.getEmail();
            if (username == null || email == null) {
                throw new NotifySBUserException("Something went wrong while creating a user.");
            }
            User checkParameter = userService.findByUsernameAndEmail(username, email);
            if (checkParameter != null) {
                NotificationHelper.notify("These username or email exists already - chose another", null);
            } else {
                String password = user.getPassword();
                if (password == null) {
                    throw new NotifySBUserException("Something went wrong while creating a user.");
                }
                User newUser = userService.createUser(username, password, email);

                this.userService.setLogin(newUser);
                this.close();
                getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
                uiEventBus.publish(this, ESBEvent.LOGIN.name());
            }
        });
        submit.setEnabled(false);

        add(userEditService, submit, close);
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
        if (e.getPayload().equals(ESBEvent.USER_COMPLETE.name())) {
            submit.setEnabled(true);
        } else if (e.getPayload().equals(ESBEvent.USER_INCOMPLETE.name())) {
            submit.setEnabled(false);
        }
    }
}
