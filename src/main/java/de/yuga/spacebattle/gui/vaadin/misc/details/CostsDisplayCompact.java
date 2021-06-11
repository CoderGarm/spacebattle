package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResolution;

import javax.annotation.Nonnull;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
@CssImport("./styles/views/main/details/compact-layout.css")
public class CostsDisplayCompact extends Div implements HasValue<AbstractField.ComponentValueChangeEvent<CostsDisplayCompact, ResourceDeposit>, ResourceDeposit> {

    @Nonnull
    private final CostsDisplayDatasource datasource;

    public CostsDisplayCompact(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        datasource = new CostsDisplayDatasource(resolution);
        addClassName("compact-block");
    }

    /**
     * Sets the resource deposits to this view. If every resource deposit was added, call update.
     *
     * @param costs the costs to add
     */
    @Override
    public void setValue(@Nonnull final ResourceDeposit costs) {
        Preconditions.checkNotNull(costs, "costs shouldn't be null!");
        Preconditions.checkArgument(EDepositType.COSTS == costs.getSubType(), "costs must be costs!");

        datasource.setValue(costs);
        datasource.resourceMap.values().forEach(this::addStyleAndAdd);
        datasource.crewMap.values().forEach(this::addStyleAndAdd);
    }

    private void addStyleAndAdd(@Nonnull final Component component) {
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        if (component instanceof HasStyle) {
            ((HasStyle) component).addClassName("block-element");
        }
        add(component);
    }

    @Override
    public ResourceDeposit getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CostsDisplayCompact, ResourceDeposit>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
