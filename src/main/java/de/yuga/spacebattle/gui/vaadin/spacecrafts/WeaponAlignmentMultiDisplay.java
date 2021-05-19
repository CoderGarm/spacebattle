package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeaponAlignmentMultiDisplay extends VerticalLayout {

    @Nonnull
    private final Map<AlignedFitting, AlignedFittingDisplay> componentMap = new HashMap<>();

    /**
     * The alignment which these weapons selection are for.
     */
    @Nonnull
    private final EWeaponAlignment weaponAlignment;

    public WeaponAlignmentMultiDisplay(@Nonnull final EWeaponAlignment weaponAlignment) {
        Preconditions.checkNotNull(weaponAlignment, "weaponAlignment shouldn't be null!");

        setClassName("module-display");

        this.weaponAlignment = weaponAlignment;
        add(new Label("Weapons fitting " + weaponAlignment.name()));
    }

    public void setValue(@Nullable final Set<AlignedFitting> value) {

        if (value == null || value.isEmpty()) {
            clearModules();
            return;
        }

        final Set<AlignedFitting> reducesFittings = value.stream().filter(a -> weaponAlignment == a.getWeaponAlignment()).collect(Collectors.toSet());

        componentMap.forEach((module, moduleEdit) -> {
            if (!reducesFittings.contains(module)) {
                remove(moduleEdit);
            }
        });

        reducesFittings.forEach(weapon -> {
            AlignedFittingDisplay moduleEdit = componentMap.get(weapon);
            if (moduleEdit == null) {
                moduleEdit = new AlignedFittingDisplay();
                componentMap.put(weapon, moduleEdit);
                add(moduleEdit);
            }
            moduleEdit.setValue(weapon);
        });
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final WeaponCountEdit[] moduleDisplaysArray = new WeaponCountEdit[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }
}
