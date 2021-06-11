package de.yuga.spacebattle.gui.vaadin.constructables.buildings;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.calculator.resource.TickOutputCalculator;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.buildings.BuildingLevelDTO;
import de.yuga.spacebattle.gui.vaadin.misc.details.misc.SimpleLabelWithCaption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConstructionOutcomeDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ConstructionOutcomeDisplay, BuildingLevelDTO>, BuildingLevelDTO> {

    @Nonnull
    private final Binder<BuildingLevelDTO> binder = new Binder<>();

    public ConstructionOutcomeDisplay() {
        final SimpleLabelWithCaption nextLevel = new SimpleLabelWithCaption("Level change");
        binder.forField(nextLevel).bind(this::getLevelText, null);

        final SimpleLabelWithCaption task = new SimpleLabelWithCaption("Task");
        binder.forField(task).bind(this::getTaskText, null);

        final SimpleLabelWithCaption output = new SimpleLabelWithCaption("Output");
        binder.forField(output).bind(this::getOutputChangeText, null);

        add(nextLevel, task, output);
    }

    @Override
    public void setValue(@Nullable final BuildingLevelDTO value) {
        binder.readBean(value);
    }

    private String getLevelText(@Nullable final BuildingLevelDTO dto) {
        if (dto == null) {
            return "";
        }
        final int nextLevel = dto.getLevel() + 1;
        return dto.getLevel() + " -> " + nextLevel + "";
    }

    private String getTaskText(@Nullable final BuildingLevelDTO dto) {
        if (dto == null) {
            return "";
        }
        final ProductionType productionType = dto.getBuilding().getProductionType();
        final EResourceType productionTarget = productionType.getProductionTarget();
        final EProductionCategory productionCategory = productionType.getProductionCategory();
        final ERefinementSequence refinementSequence = productionType.getRefinementSequence();
        // todo how to generify output task?
        return productionCategory.name();
    }

    /**
     * Creates the from-to string which represents the current construction output and the output at the next level.
     *
     * @param dto the parameters
     * @return the string
     */
    private String getOutputChangeText(@Nullable final BuildingLevelDTO dto) {
        if (dto == null) {
            return "";
        }
        final ProductionType productionType = dto.getBuilding().getProductionType();
        final EResourceType productionTarget = productionType.getProductionTarget();
        final long currentOutput;
        final long nextLevelOutput;
        if (productionTarget == EResourceType.POPULATION) {
            currentOutput = TickOutputCalculator.getTickOutputByLevelForPopulation(dto.getPlanet(), dto.getBuilding(), dto.getLevel()).longValue();
            nextLevelOutput = TickOutputCalculator.getTickOutputByLevelForPopulation(dto.getPlanet(), dto.getBuilding(), dto.getLevel() + 1).longValue();
        } else {
            currentOutput = TickOutputCalculator.getTickOutputByLevel(dto.getPlanet(), dto.getBuilding(), dto.getLevel());
            nextLevelOutput = TickOutputCalculator.getTickOutputByLevel(dto.getPlanet(), dto.getBuilding(), dto.getLevel() + 1);
        }
        return currentOutput + " -> " + nextLevelOutput;
    }

    @Override
    public BuildingLevelDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ConstructionOutcomeDisplay, BuildingLevelDTO>> listener) {
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
