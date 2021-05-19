package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Weapon;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.WeaponAlignmentDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaponAlignmentMultiEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<WeaponAlignmentMultiEdit, Set<WeaponAlignmentDTO>>, Set<WeaponAlignmentDTO>> {

    @Nonnull
    private final Map<Weapon, WeaponCountEdit> componentMap = new HashMap<>();

    /**
     * The alignment which these weapons selection are for.
     */
    @Nonnull
    private final EWeaponAlignment weaponAlignment;

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<WeaponAlignmentMultiEdit, Set<WeaponAlignmentDTO>>> valueChangeListener;

    public WeaponAlignmentMultiEdit(@Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        setClassName("module-display");

        this.weaponAlignment = weaponAlignment;
        add(new Label("Weapons fitting selection " + weaponAlignment.name()));
    }

    @Override
    public void setValue(@Nullable final Set<WeaponAlignmentDTO> value) {

        if (value == null || value.isEmpty()) {
            clearModules();
            return;
        }

        final Map<Weapon, Integer> modules = value.stream()
                .filter(v -> v.getAllowedWeaponAlignments().contains(weaponAlignment))
                .collect(Collectors.toMap(WeaponAlignmentDTO::getWeapon, WeaponAlignmentDTO::getCountNumeric));

        componentMap.forEach((module, moduleEdit) -> {
            if (!modules.containsKey(module)) {
                remove(moduleEdit);
            }
        });

        modules.forEach((weapon, amount) -> {
            WeaponCountEdit moduleEdit = componentMap.get(weapon);
            final WeaponAlignmentDTO weaponAlignmentDTO = new WeaponAlignmentDTO(weapon, amount);
            weaponAlignmentDTO.setSelectedWeaponAlignment(weaponAlignment);
            if (moduleEdit == null) {
                moduleEdit = new WeaponCountEdit();
                componentMap.put(weapon, moduleEdit);
                moduleEdit.addValueChangeListener(event -> fireChangeEvent());
                add(moduleEdit);
            }
            moduleEdit.setValue(weaponAlignmentDTO);
        });
    }


    private void fireChangeEvent() {
        if (valueChangeListener != null) {
            final AbstractField.ComponentValueChangeEvent<WeaponAlignmentMultiEdit, Set<WeaponAlignmentDTO>> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true);
            valueChangeListener.valueChanged(changeEvent);
        }
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final WeaponCountEdit[] moduleDisplaysArray = new WeaponCountEdit[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }

    @Override
    public Set<WeaponAlignmentDTO> getValue() {
        return componentMap.keySet().stream()
                .map(module -> componentMap.get(module).getValue())
                .filter(dto -> dto.getCountNumeric() > 0)
                .collect(Collectors.toSet());
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<WeaponAlignmentMultiEdit, Set<WeaponAlignmentDTO>>> listener) {
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
