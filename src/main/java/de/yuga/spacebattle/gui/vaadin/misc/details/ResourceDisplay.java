package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

public class ResourceDisplay extends HorizontalLayout {

    public ResourceDisplay(@Nonnull final ResourceDeposit resourceDeposit) {
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit shouldn't be null!");

        Map<EResourceType, BigDecimal> resources = resourceDeposit.getResources();
        resources.forEach((resourceType, amount) -> {
            ResourceElementDisplay resourceElementDisplay = new ResourceElementDisplay(resourceType, amount);
            add(resourceElementDisplay);
        });
    }
}
