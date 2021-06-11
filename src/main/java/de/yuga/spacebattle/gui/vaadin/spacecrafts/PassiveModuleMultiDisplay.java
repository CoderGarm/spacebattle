package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.PassiveModuleCountDTO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PassiveModuleMultiDisplay extends VerticalLayout {

    @Nonnull
    private final Map<PassiveModuleCountDTO, SupportFittingDisplay> componentMap = new HashMap<>();

    public PassiveModuleMultiDisplay() {
        setClassName("module-display");

        add(new Label("Support modules"));
    }

    public void setValue(@Nullable final Set<PassiveModuleCountDTO> value) {

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
            SupportFittingDisplay display = componentMap.get(ammunitionModuleCountDTO);
            if (display == null) {
                display = new SupportFittingDisplay();
                componentMap.put(ammunitionModuleCountDTO, display);
                add(display);
            }
            display.setValue(ammunitionModuleCountDTO);
        });
    }

    private void clearModules() {
        if (!componentMap.values().isEmpty()) {
            final SupportFittingDisplay[] moduleDisplaysArray = new SupportFittingDisplay[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
        }
    }
}
