package de.yuga.spacebattle.gui.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class ResourceElementDisplay extends VerticalLayout {


    public ResourceElementDisplay(@Nonnull final EResourceType resourceType, @Nonnull final BigDecimal amount) {
        Preconditions.checkNotNull(resourceType, "resourceType shouldn't be null!");
        Preconditions.checkNotNull(amount, "amount shouldn't be null!");

        H6 subjectDisplay = new H6(resourceType.getSingularName());
        H6 amountDisplay = new H6(amount.toString());
        add(subjectDisplay, amountDisplay);
    }
}
