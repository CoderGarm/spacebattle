package de.yuga.spacebattle.gui.vaadin.account.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.validators.base.CustomValidatorFactory;
import de.yuga.spacebattle.gui.vaadin.events.ESBEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.Event;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@SpringComponent
@UIScope
public class UserEditService extends VerticalLayout {

    @Nonnull
    private final UserObject user = new UserObject();

    @Nonnull
    private final TextField username;

    @Nonnull
    private final TextField password;

    @Nonnull
    private final TextField email;

    @Nonnull
    private final RaceSelect raceSelector;

    @Nonnull
    private final EventBus.UIEventBus uiEventBus;

    @Autowired
    public UserEditService(@Nonnull final EventBus.UIEventBus uiEventBus) {
        Preconditions.checkNotNull(uiEventBus, "uiEventBus shouldn't be null!");

        this.uiEventBus = uiEventBus;
        this.uiEventBus.subscribe(this);

        this.username = new TextField("Username");
        this.password = new TextField("Password");
        this.email = new TextField("eMail");
        this.raceSelector = new RaceSelect();

        this.username.addValueChangeListener(u -> {
            sendEvent(this.user.setUsername(u.getValue()));
        });
        this.password.addValueChangeListener(u -> {
            sendEvent(this.user.setPassword(u.getValue()));
        });
        this.email.addValueChangeListener(u -> {
            sendEvent(this.user.setEmail(u.getValue()));
        });
        this.raceSelector.addValueChangeListener(u -> {
            sendEvent(this.user.setRaceType(u.getValue()));
        });

        add(username, password, raceSelector, email);
    }

    private void sendEvent(final boolean userIsValid) {
        if (userIsValid) {
            this.uiEventBus.publish(this, ESBEvent.USER_COMPLETE.name());
        } else {
            this.uiEventBus.publish(this, ESBEvent.USER_INCOMPLETE.name());
        }
    }

    @Nonnull
    public UserObject getUser() {
        return user;
    }

    @Nonnull
    public TextField getUsername() {
        return username;
    }

    @Nonnull
    public TextField getPassword() {
        return password;
    }

    @Nonnull
    public TextField getEmail() {
        return email;
    }

    @Nonnull
    public RaceSelect getRaceSelector() {
        return raceSelector;
    }

    @EventBusListenerMethod
    protected void onEvent(Event<String> e) {
    }

    public static class UserObject {

        @Nullable
        @NotNull(message = "username must not be null")
        @Size(min = 1, max = 30)
        private String username;

        @Nullable
        @NotNull(message = "password must not be null")
        @Size(min = 1, max = 50)
        private String password;

        @Nullable
        @NotNull(message = "eMail must not be null")
        @Size(min = 1, max = 50)
        private String email;

        @Nullable
        @NotNull(message = "racetype must not be null")
        private ERaceType raceType;

        private final Validator validator = CustomValidatorFactory.buildCustomValidator();

        public UserObject() {
        }

        public boolean isValid() {
            Set<ConstraintViolation<UserObject>> validate = validator.validate(this);
            if (validate.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

        @Nullable
        public String getUsername() {
            return username;
        }

        public boolean setUsername(@Nullable final String username) {
            this.username = username;
            return this.isValid();
        }

        @Nullable
        public String getPassword() {
            return password;
        }

        public boolean setPassword(@Nullable final String password) {
            this.password = password;
            return this.isValid();
        }

        @Nullable
        public String getEmail() {
            return email;
        }

        public boolean setEmail(@Nullable final String email) {
            this.email = email;
            return this.isValid();
        }

        @Nullable
        public ERaceType getRaceType() {
            return raceType;
        }

        public boolean setRaceType(@Nullable final ERaceType raceType) {
            this.raceType = raceType;
            return this.isValid();
        }
    }
}
