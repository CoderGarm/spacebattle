package de.yuga.spacebattle.gui.vaadin.turn.resource.crew;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.crew.CrewRequirementDTO;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResolution;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CrewDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<CrewDisplay, CrewRequirementDTO>, CrewRequirementDTO> {

    @Nullable
    private CrewRequirementDTO crewRequirementDTO;

    @Nonnull
    private final Map<EEducationType, CrewIconDisplaySingle> content = new HashMap<>();

    @Nonnull
    private final EResolution resolution;

    public CrewDisplay(@Nonnull final EResolution resolution) {
        Preconditions.checkNotNull(resolution, "resolution shouldn't be null!");

        this.resolution = resolution;
    }

    @Override
    public void setValue(@Nonnull final CrewRequirementDTO value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        crewRequirementDTO = value;
        for (final EEducationType educationType : EEducationType.values()) {
            final long amount = value.getCrewAmountByType(educationType);
            CrewIconDisplaySingle crewIconDisplaySingle = content.get(educationType);
            if (amount != 0) {
                if (crewIconDisplaySingle == null) {
                    crewIconDisplaySingle = new CrewIconDisplaySingle(resolution);
                    content.put(educationType, crewIconDisplaySingle);
                    add(crewIconDisplaySingle);
                }
                crewIconDisplaySingle.setValue(new CrewIconAmountDTO(educationType, amount));
            } else if (crewIconDisplaySingle != null) {
                // remove if not longer present
                remove(crewIconDisplaySingle);
                content.remove(educationType);
            }
        }
    }

    @Nullable
    @Override
    public CrewRequirementDTO getValue() {
        return crewRequirementDTO;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CrewDisplay, CrewRequirementDTO>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
