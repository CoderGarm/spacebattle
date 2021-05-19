package de.yuga.spacebattle.gui.vaadin.account.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;

public class UserDisplay extends HorizontalLayout {

    @Nonnull
    private final User user;

    @Nonnull
    private final TextField username;

    @Nonnull
    private final TextField password;

    @Nonnull
    private final TextField email;

    public UserDisplay(@Nonnull User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        this.user = user;

        this.username = new TextField("Username");
        this.username.setValue(user.getUsername());
        this.username.setReadOnly(true);

        this.password = new TextField("Password");
        this.password.setValue(user.getPassword());
        this.password.setReadOnly(true);

        this.email = new TextField("eMail");
        this.email.setValue(user.getEmail());
        this.email.setReadOnly(true);

        add(username, password, email);
    }
}
