package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.AmmunitionModule;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.AmmunitionModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AmmunitionModuleMultiEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<AmmunitionModuleMultiEdit, Set<AmmunitionModuleCountDTO>>, Set<AmmunitionModuleCountDTO>> {

    @Nonnull
    private final Map<AmmunitionModule, AmmunitionModuleCountEdit> componentMap = new HashMap<>();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<AmmunitionModuleMultiEdit, Set<AmmunitionModuleCountDTO>>> valueChangeListener;

    public AmmunitionModuleMultiEdit() {
        setClassName("module-display");
        add(new Label("Ammunition selection"));
    }

    @Override
    public void setValue(@Nullable final Set<AmmunitionModuleCountDTO> value) {

        if (value == null || value.isEmpty()) {
            clearModules();
            return;
        }

        final Map<AmmunitionModule, Integer> modules = value.stream()
                .collect(Collectors.toMap(AmmunitionModuleCountDTO::getAmmunitionModule, AmmunitionModuleCountDTO::getCount));

        componentMap.forEach((module, moduleEdit) -> {
            if (!modules.containsKey(module)) {
                remove(moduleEdit);
            }
        });

        modules.forEach((ammunitionModule, amount) -> {
            AmmunitionModuleCountEdit moduleEdit = componentMap.get(ammunitionModule);
            final AmmunitionModuleCountDTO weaponAlignmentCountDTO = new AmmunitionModuleCountDTO(ammunitionModule, amount);
            if (moduleEdit == null) {
                moduleEdit = new AmmunitionModuleCountEdit();
                componentMap.put(ammunitionModule, moduleEdit);
                moduleEdit.addValueChangeListener(event -> fireChangeEvent());
                add(moduleEdit);
            }
            moduleEdit.setValue(weaponAlignmentCountDTO);
        });
    }


    private void fireChangeEvent() {
        if (valueChangeListener != null) {
            final AbstractField.ComponentValueChangeEvent<AmmunitionModuleMultiEdit, Set<AmmunitionModuleCountDTO>> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true);
            valueChangeListener.valueChanged(changeEvent);
        }
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final AmmunitionModuleCountEdit[] moduleDisplaysArray = new AmmunitionModuleCountEdit[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }

    @Override
    public Set<AmmunitionModuleCountDTO> getValue() {
        return componentMap.keySet().stream()
                .map(module -> componentMap.get(module).getValue())
                .filter(dto -> dto.getCount() > 0)
                .collect(Collectors.toSet());
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<AmmunitionModuleMultiEdit, Set<AmmunitionModuleCountDTO>>> listener) {
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
