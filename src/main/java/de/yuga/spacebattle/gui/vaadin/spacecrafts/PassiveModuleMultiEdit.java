package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.PassiveModule;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.PassiveModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PassiveModuleMultiEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<PassiveModuleMultiEdit, Set<PassiveModuleCountDTO>>, Set<PassiveModuleCountDTO>> {

    @Nonnull
    private final Map<PassiveModule, PassiveModuleCountEdit> componentMap = new HashMap<>();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PassiveModuleMultiEdit, Set<PassiveModuleCountDTO>>> valueChangeListener;

    public PassiveModuleMultiEdit() {
        setClassName("module-display");
        add(new Label("Support module selection"));
    }

    @Override
    public void setValue(@Nullable final Set<PassiveModuleCountDTO> value) {

        if (value == null || value.isEmpty()) {
            clearModules();
            return;
        }

        final Map<PassiveModule, Integer> modules = value.stream()
                .collect(Collectors.toMap(PassiveModuleCountDTO::getPassiveModule, PassiveModuleCountDTO::getCountNumeric));

        componentMap.forEach((module, moduleEdit) -> {
            if (!modules.containsKey(module)) {
                remove(moduleEdit);
            }
        });

        modules.forEach((ammunitionModule, amount) -> {
            PassiveModuleCountEdit moduleEdit = componentMap.get(ammunitionModule);
            final PassiveModuleCountDTO weaponAlignmentCountDTO = new PassiveModuleCountDTO(ammunitionModule, amount);
            if (moduleEdit == null) {
                moduleEdit = new PassiveModuleCountEdit();
                componentMap.put(ammunitionModule, moduleEdit);
                moduleEdit.addValueChangeListener(event -> fireChangeEvent());
                add(moduleEdit);
            }
            moduleEdit.setValue(weaponAlignmentCountDTO);
        });
    }


    private void fireChangeEvent() {
        if (valueChangeListener != null) {
            final AbstractField.ComponentValueChangeEvent<PassiveModuleMultiEdit, Set<PassiveModuleCountDTO>> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true);
            valueChangeListener.valueChanged(changeEvent);
        }
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final PassiveModuleCountEdit[] moduleDisplaysArray = new PassiveModuleCountEdit[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }

    @Override
    public Set<PassiveModuleCountDTO> getValue() {
        return componentMap.keySet().stream()
                .map(module -> componentMap.get(module).getValue())
                .filter(dto -> dto.getCountNumeric() > 0)
                .collect(Collectors.toSet());
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<PassiveModuleMultiEdit, Set<PassiveModuleCountDTO>>> listener) {
        this.valueChangeListener = listener;
        return new Registration() {
            @Override
            public void remove() {
                valueChangeListener = null;
            }
        };
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
