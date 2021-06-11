package de.yuga.spacebattle.gui.vaadin.misc.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.EResolution;

import javax.annotation.Nonnull;

/**
 * Displays the name and amount of the yield factors at the given planet.
 */
public class CostsDisplayVertical extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<CostsDisplayVertical, ResourceDeposit>, ResourceDeposit> {

    @Nonnull
    private final CostsDisplayDatasource datasource;

    public CostsDisplayVertical(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        datasource = new CostsDisplayDatasource(resolution);
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
        datasource.resourceMap.values().forEach(this::add);
        datasource.crewMap.values().forEach(this::add);
    }

    @Override
    public ResourceDeposit getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CostsDisplayVertical, ResourceDeposit>> listener) {
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
