package de.yuga.spacebattle.gui.vaadin.turn.action;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.gui.vaadin.misc.details.SimpleValueChangeEvent;
import de.yuga.spacebattle.gui.vaadin.orbitals.details.PlanetDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Displays a fleet in motion represented by the {@link MoveDTO}.
 */
@CssImport("./styles/views/main/details/move-edit.css")
public class MoveEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<MoveEdit, MoveDTO>, MoveDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveEdit.class);

    @Nonnull
    private final Binder<MoveDTO> binder = new Binder<>(MoveDTO.class);

    @Nullable
    private ValueChangeListener<SimpleValueChangeEvent> valueChangeListener;

    @Nonnull
    private final Button cancelFlight;

    public MoveEdit() {
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
        planetDisplay.setClassName("planet-display");

        final Label timeToTravelLabel = new Label();
        final ReadOnlyHasValue<String> timeToTravelLabelText = new ReadOnlyHasValue<>(timeToTravelLabel::setText);
        binder.forField(timeToTravelLabelText).bind(MoveDTO::getTimeToTravel, null);

        cancelFlight = new Button("Cancel flight", event -> {
            final MoveDTO moveDTO = getValue();
            if (moveDTO != null) {
                moveDTO.setCancelFlight();
                binder.readBean(moveDTO);
            }
            event.getSource().setEnabled(false);
            if (valueChangeListener == null) {
                return;
            }
            valueChangeListener.valueChanged(new SimpleValueChangeEvent());
        });

        cancelFlight.setEnabled(false);

        final HorizontalLayout horizontalWrapper = new HorizontalLayout();
        horizontalWrapper.add(targetLabel, planetDisplay, timeToTravelLabel);

        add(horizontalWrapper, cancelFlight);
    }

    @Override
    public void setValue(MoveDTO value) {
        binder.setBean(value);
        if (value != null && value.isInMotion()) {
            cancelFlight.setEnabled(true);
        }
        if (valueChangeListener == null) {
            return;
        }
        valueChangeListener.valueChanged(new SimpleValueChangeEvent());
    }

    @Nullable
    @Override
    public MoveDTO getValue() {
        return binder.getBean();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<MoveEdit, MoveDTO>> listener) {
        // not necessary
        return null;
    }

    public Registration addChangeListener(@Nonnull final ValueChangeListener<SimpleValueChangeEvent> listener) {
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
