package de.yuga.spacebattle.gui.vaadin.combined.account;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;

import javax.annotation.Nonnull;

public class AllianceDisplay extends VerticalLayout {

    public AllianceDisplay(@Nonnull final Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        H5 name = new H5(alliance.getName());
        H5 code = new H5(alliance.getCode());

        add(name, code);
    }
}
