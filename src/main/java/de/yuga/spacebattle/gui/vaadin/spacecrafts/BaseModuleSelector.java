package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BaseModuleSelector extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<BaseModuleSelector, Collection<BaseModule>>, Collection<BaseModule>>, HasValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseModuleSelector.class);

    /**
     * The index where the display should be added.
     */
    private static final int DISPLAY_INDEX = 1;

    @Nonnull
    private final Binder<BaseModule> binder = new Binder<>();

    @Nonnull
    private final RadioButtonGroup<BaseModule> selectGroup;

    public BaseModuleSelector(@Nonnull final String label) {
        Preconditions.checkNotNull(label, "label shouldn't be null!");

        setClassName("module-display");

        selectGroup = new RadioButtonGroup<>();
        selectGroup.setClassName("module-display");

        final Button clear = new Button("Clear selection", event -> {
            event.getSource().setEnabled(false);
            selectGroup.clear();
        });
        clear.setEnabled(false);
        clear.setClassName("base-module-selector-button");


        selectGroup.setLabel(label);
        selectGroup.setRenderer(new TextRenderer<>(hull -> {
            final StringBuilder sb = new StringBuilder();
            sb.append(hull.getName()).append(", ").append(hull.getDescription());
            sb.append(", Level ").append(hull.getTechLevel());
            sb.append(", Use capacity ").append(hull.getUseCapacity());
            return sb.toString();
        }));
        selectGroup.addThemeVariants(RadioGroupVariant.MATERIAL_VERTICAL);

        final BaseModuleSelectorDisplay display = new BaseModuleSelectorDisplay();
        final ReadOnlyHasValue<BaseModule> hullSelectedReadOnly = new ReadOnlyHasValue<>(display::update);
        binder.forField(hullSelectedReadOnly).bind(hull -> {
            clear.setEnabled(true);
            addComponentAtIndex(DISPLAY_INDEX, display);
            return hull;
        }, null);

        HorizontalLayout layout = new HorizontalLayout();
        layout.add(selectGroup, clear);

        add(layout);
    }

    /**
     * Will update the display or clear all fields.
     *
     * @param value the baseModules to display
     */
    @Override
    public void setValue(Collection<BaseModule> value) {
        if (value != null) {
            selectGroup.setItems(value);
        } else {
            selectGroup.clear();
        }
    }

    public void preselect(@Nullable final BaseModule baseModule) {
        if (baseModule == null) {
            return;
        }

        selectGroup.setValue(baseModule);
    }

    @Override
    public List<BaseModule> getValue() {
        final List<BaseModule> list = new ArrayList<>();
        list.add(selectGroup.getValue());
        return list;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<BaseModuleSelector, Collection<BaseModule>>> listener) {

        return selectGroup.addValueChangeListener(event -> {
            final BaseModule selectedHull = event.getValue();
            final ArrayList<BaseModule> baseModules = new ArrayList<>();
            baseModules.add(selectedHull);
            binder.readBean(selectedHull);
            final AbstractField.ComponentValueChangeEvent<BaseModuleSelector, Collection<BaseModule>> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, baseModules, false);
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
