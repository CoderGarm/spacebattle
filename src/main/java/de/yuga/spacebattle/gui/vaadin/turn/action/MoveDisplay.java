package de.yuga.spacebattle.gui.vaadin.turn.action;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.misc.details.SBValueChangeEvent;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Displays a fleet in motion represented by the {@link MoveDTO}.
 */
public class MoveDisplay extends HorizontalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<MoveDisplay, MoveDTO>, MoveDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveDisplay.class);

    @Nonnull
    private final Binder<MoveDTO> binder = new Binder<>(MoveDTO.class);

    @Nullable
    private ValueChangeListener<SBValueChangeEvent> valueChangeListener;

    public MoveDisplay() {
        final Label targetLabel = new Label();
        final ReadOnlyHasValue<String> targetLabelText = new ReadOnlyHasValue<>(targetLabel::setText);
        binder.forField(targetLabelText).bind(moveDTO -> {
            if (moveDTO == null) {
                return "No Movement";
            }
            if (moveDTO.isInMotion()) {
                return "In motion";

            }
            return "Planning movement";
        }, null);

        final PlanetDisplay planetDisplay = new PlanetDisplay();
        binder.forField(planetDisplay).bind(MoveDTO::getTarget, null);

        final Label timeToTravelLabel = new Label();
        final ReadOnlyHasValue<String> timeToTravelLabelText = new ReadOnlyHasValue<>(timeToTravelLabel::setText);
        binder.forField(timeToTravelLabelText).bind(MoveDTO::getTimeToTravel, null);

        add(targetLabel, planetDisplay, timeToTravelLabel);
    }

    @Override
    public void setValue(MoveDTO value) {
        binder.setBean(value);
        if (valueChangeListener == null) {
            return;
        }
        valueChangeListener.valueChanged(new SBValueChangeEvent());
    }

    @Nullable
    @Override
    public MoveDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<MoveDisplay, MoveDTO>> listener) {
        // not necessary
        return null;
    }

    public Registration addChangeListener(@Nonnull final ValueChangeListener<SBValueChangeEvent> listener) {
        Preconditions.checkNotNull(listener, "listener shouldn't be null!");

        valueChangeListener = listener;
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
