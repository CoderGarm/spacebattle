package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HullSelector extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<HullSelector, Collection<Hull>>, Collection<Hull>>, HasValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(HullSelector.class);

    @Nonnull
    private final Binder<Hull> hullBinder = new Binder<>(Hull.class);

    @Nonnull
    private final RadioButtonGroup<Hull> selectGroup;

    public HullSelector() {
        setClassName("module-display");

        selectGroup = new RadioButtonGroup<>();
        selectGroup.setClassName("module-display");

        selectGroup.setLabel("Hull selection");
        selectGroup.setRenderer(new TextRenderer<>(hull -> {
            final StringBuilder sb = new StringBuilder();
            sb.append(hull.getName()).append(", ").append(hull.getDescription());
            sb.append(", Level ").append(hull.getLevel());
            sb.append(", Capacity ").append(hull.getConstructionCapacity());
            return sb.toString();
        }));
        selectGroup.addThemeVariants(RadioGroupVariant.MATERIAL_VERTICAL);

        final HullDisplay hullDisplay = new HullDisplay();
        final ReadOnlyHasValue<Hull> hullSelectedReadOnly = new ReadOnlyHasValue<>(hullDisplay::update);
        hullBinder.forField(hullSelectedReadOnly).bind(hull -> hull, null);

        add(selectGroup, hullDisplay);
    }

    /**
     * Will update the display or clear all fields.
     *
     * @param hulls the hulls to display
     */
    public void update(@Nullable final Collection<Hull> hulls) {
        if (hulls != null) {
            selectGroup.setItems(hulls);
        } else {
            selectGroup.clear();
        }
    }


    @Override
    public void setValue(Collection<Hull> value) {
        this.update(value);
    }

    @Override
    public List<Hull> getValue() {
        final List<Hull> hullList = new ArrayList<>();
        hullList.add(selectGroup.getValue());
        return hullList;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<HullSelector, Collection<Hull>>> listener) {

        return selectGroup.addValueChangeListener(event -> {
            final Hull selectedHull = event.getValue();
            ArrayList<Hull> hulls = new ArrayList<>();
            hulls.add(selectedHull);
            hullBinder.readBean(selectedHull);
            final AbstractField.ComponentValueChangeEvent<HullSelector, Collection<Hull>> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, hulls, false);
            listener.valueChanged(changeEvent);
        });
    }


    @Override
    public void setReadOnly(boolean readOnly) {
        selectGroup.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return selectGroup.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        selectGroup.setRequiredIndicatorVisible(true);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return selectGroup.isRequiredIndicatorVisible();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        selectGroup.setErrorMessage(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return selectGroup.getErrorMessage();
    }

    @Override
    public void setInvalid(boolean invalid) {
        selectGroup.setInvalid(invalid);
    }

    @Override
    public boolean isInvalid() {
        return selectGroup.isInvalid();
    }
}
