package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingLevelDTO;
import de.yuga.spacebattle.gui.vaadin.misc.details.CostsDisplayVertical;

import javax.annotation.Nonnull;

public class ConstructionCostsAndStatsComparableDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ConstructionCostsAndStatsComparableDisplay, BuildingLevelDTO>, BuildingLevelDTO> {

    @Nonnull
    private final Binder<BuildingLevelDTO> binder = new Binder<>();

    public ConstructionCostsAndStatsComparableDisplay() {
        final CostsDisplayVertical costsDisplayVertical = new CostsDisplayVertical(EResolution.PX24);
        binder.forField(costsDisplayVertical).bind(b -> JobCostsCalculator.getCostsForLevel(b.getBuilding().getCosts(), b.getLevel()), null);

        final ConstructionOutcomeDisplay outputDisplay = new ConstructionOutcomeDisplay();
        binder.forField(outputDisplay).bind(b -> b, null);

        // todo display left-over-deposit?
        add(outputDisplay, costsDisplayVertical);
    }


    @Override
    public void setValue(BuildingLevelDTO value) {
        binder.readBean(value);
    }

    @Override
    public BuildingLevelDTO getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ConstructionCostsAndStatsComparableDisplay, BuildingLevelDTO>> listener) {
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
