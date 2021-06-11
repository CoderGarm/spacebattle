package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.AmmunitionModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AmmunitionModuleMultiDisplay extends VerticalLayout {

    @Nonnull
    private final Map<AmmunitionModuleCountDTO, AmmunitionFittingDisplay> componentMap = new HashMap<>();

    public AmmunitionModuleMultiDisplay() {
        setClassName("module-display");

        add(new Label("Ammunition fitting"));
    }

    public void setValue(@Nullable final Set<AmmunitionModuleCountDTO> value) {

        if (value == null || value.isEmpty()) {
            clearModules();
            return;
        }

        componentMap.forEach((module, moduleEdit) -> {
            if (!value.contains(module)) {
                remove(moduleEdit);
            }
        });

        value.forEach(ammunitionModuleCountDTO -> {
            AmmunitionFittingDisplay display = componentMap.get(ammunitionModuleCountDTO);
            if (display == null) {
                display = new AmmunitionFittingDisplay();
                componentMap.put(ammunitionModuleCountDTO, display);
                add(display);
            }
            display.setValue(ammunitionModuleCountDTO);
        });
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final AmmunitionFittingDisplay[] moduleDisplaysArray = new AmmunitionFittingDisplay[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }
}
