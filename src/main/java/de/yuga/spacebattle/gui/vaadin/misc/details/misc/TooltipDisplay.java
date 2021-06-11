package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import com.google.common.base.Preconditions;
import com.vaadin.componentfactory.Tooltip;
import com.vaadin.componentfactory.TooltipAlignment;
import com.vaadin.componentfactory.TooltipPosition;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.theme.material.Material;

import javax.annotation.Nonnull;

/**
 * The tool tip element.<br>
 * It will be displayed to the bottom-right of the attached element.
 */
@CssImport(value = "./styles/views/main/details/tooltip.css", themeFor = "vcf-tooltip")
public class TooltipDisplay extends Tooltip {

    public TooltipDisplay(@Nonnull final HasComponents parent,
                          @Nonnull final Component attachTo,
                          @Nonnull final Component toDisplay) {
        Preconditions.checkNotNull(parent, "parent shouldn't be null!");
        Preconditions.checkNotNull(attachTo, "attachTo shouldn't be null!");
        Preconditions.checkNotNull(toDisplay, "toDisplay shouldn't be null!");

        attachToComponent(attachTo);
        setPosition(TooltipPosition.RIGHT);
        setAlignment(TooltipAlignment.BOTTOM);
        addThemeName(Material.DARK);
        add(toDisplay);
        parent.add(this);
    }
}
